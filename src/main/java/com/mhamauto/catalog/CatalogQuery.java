package com.mhamauto.catalog;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Immutable, bounded, explicitly validated GET search/filter/pagination input. */
public record CatalogQuery(String query, Long vendorId, Long categoryId, boolean uncategorized,
                           String sort, int page) {
    public static final int PAGE_SIZE = 24;
    private static final Pattern WORD = Pattern.compile("[\\p{L}\\p{N}]{2,}");
    private static final Set<String> SORTS = Set.of("relevance", "title_asc", "title_desc", "price_asc", "price_desc");

    public static CatalogQuery parse(String q, String vendor, String category, String sort, String page) {
        String search = q == null ? "" : q.strip();
        if (search.length() > 80 || search.chars().anyMatch(ch -> Character.isISOControl(ch)))
            throw new IllegalArgumentException("Search must be at most 80 characters with no control characters");
        Long v = id(vendor, "Vendor");
        boolean missingCategory = "uncategorized".equals(category);
        Long c = missingCategory ? null : id(category, "Category");
        String order = sort == null || sort.isBlank() ? "relevance" : sort;
        if (!SORTS.contains(order)) throw new IllegalArgumentException("Invalid sort choice");
        int p = 1;
        if (page != null && !page.isBlank()) {
            if (!page.matches("[1-9][0-9]{0,4}")) throw new IllegalArgumentException("Invalid page number");
            p = Integer.parseInt(page);
            if (p > 10000) throw new IllegalArgumentException("Page number too large");
        }
        return new CatalogQuery(search, v, c, missingCategory, order, p);
    }
    private static Long id(String value, String label) {
        if (value == null || value.isBlank()) return null;
        if (!value.matches("[1-9][0-9]{0,17}")) throw new IllegalArgumentException("Invalid " + label.toLowerCase(Locale.ROOT));
        try { return Long.parseLong(value); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("Invalid " + label.toLowerCase(Locale.ROOT)); }
    }
    public String getQuery(){return query;}
    public String getSort(){return sort;}
    public Long getVendorId(){return vendorId;}
    public Long getCategoryId(){return categoryId;}
    public boolean getUncategorized(){return uncategorized;}
    public String getVendorValue(){return vendorValue();}
    public String getCategoryValue(){return categoryValue();}
    public String getPreviousHref(){return href(Math.max(1,page-1));}
    public String getNextHref(){return href(page+1);}
    public int offset() { return (page - 1) * PAGE_SIZE; }
    public String fullTextTerms() {
        Matcher matcher = WORD.matcher(query);
        StringBuilder result = new StringBuilder(); int count = 0;
        while (matcher.find() && count++ < 6) {
            if (!result.isEmpty()) result.append(' ');
            result.append('+').append(matcher.group()).append('*');
        }
        return result.toString();
    }
    /** Escape MySQL LIKE wildcards; append only an intentional final prefix wildcard. */
    public String skuPrefix() { return query.replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%"; }
    public String href(int toPage) {
        return "/products?q=" + encode(query) + "&vendor=" + (vendorId == null ? "" : vendorId) +
              "&category=" + (uncategorized ? "uncategorized" : categoryId == null ? "" : categoryId) +
              "&sort=" + encode(sort) + "&page=" + toPage;
    }
    private static String encode(String value) { return URLEncoder.encode(value, StandardCharsets.UTF_8); }
    public String vendorValue() { return vendorId == null ? "" : vendorId.toString(); }
    public String categoryValue() { return uncategorized ? "uncategorized" : categoryId == null ? "" : categoryId.toString(); }
}
