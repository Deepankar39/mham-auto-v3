package com.mhamauto.importer;

import com.mhamauto.media.ExternalImageUrlPolicy;
import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** Immutable source row; preserve SKU apostrophes, HTML and prices exactly as supplied. */
public record ShopifyRecord(long recordNumber, Map<String,String> fields) {
    public ShopifyRecord { fields = Map.copyOf(fields); }
    public String field(String name) { return fields.getOrDefault(name, ""); }
    public String trim(String name) { return field(name).trim(); }

    public String handle() { return trim("Handle"); }
    public String sku() { return field("Variant SKU"); }
    public String title() { return trim("Title"); }

    public static Boolean optionalBoolean(String raw) {
        if (raw == null || raw.isBlank()) return null;
        if (raw.equalsIgnoreCase("TRUE")) return true;
        if (raw.equalsIgnoreCase("FALSE")) return false;
        throw new IllegalArgumentException("Invalid TRUE/FALSE value");
    }

    public static Integer optionalNonnegativeInt(String raw, String name) {
        if (raw == null || raw.isBlank()) return null;
        try {
            int value = Integer.parseInt(raw.trim());
            if (value < 0) throw new IllegalArgumentException(name + " cannot be negative");
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(name + " must be a whole number", e);
        }
    }

    public BigDecimal kwdPrice(String currency) {
        String raw = trim("Variant Price");
        if (raw.isEmpty()) return null;
        BigDecimal price;
        try {
            price = new BigDecimal(raw);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Variant Price is not decimal", ex);
        }
        if (price.signum() < 0 || price.scale() > 3 || price.precision() > 18) {
            throw new IllegalArgumentException("Variant Price must be nonnegative and fit DECIMAL(18,3)");
        }
        return "KWD".equals(currency) ? price : null;
    }

    public List<String> warningMessages() {
        List<String> warnings = new ArrayList<>();
        for (String field : List.of("Image Src", "Variant Image")) {
            if (!trim(field).isEmpty() && !ExternalImageUrlPolicy.isAllowed(trim(field))) {
                warnings.add(field + " skipped: requires an HTTPS cdn.shopify.com URL");
            }
        }
        if (sku().startsWith("'")) warnings.add("SKU starts with apostrophe: preserved verbatim");
        if (sku().isBlank() && !trim("Variant Image").isBlank()) {
            warnings.add("Variant Image has no SKU on this row; image retained as product media only");
        }
        if (!trim("Image Src").isEmpty() && trim("Image Alt Text").equals("belt-tensioner-mini-cooper-clubman-2016")) {
            warnings.add("Image alt text appears unrelated: preserved, requires manual correction");
        }
        return Collections.unmodifiableList(warnings);
    }

    /** Only validates a single CSV row; cross-row associations are handled by the DAO. */
    public void validate(String currency) {
        if (handle().isEmpty() || handle().length() > 255) throw new IllegalArgumentException("Handle missing/too long");
        if (title().length() > 512) throw new IllegalArgumentException("Title too long");
        if (sku().length() > 191) throw new IllegalArgumentException("Variant SKU too long");
        if (!sku().equals(sku().strip())) throw new IllegalArgumentException("Variant SKU has surrounding whitespace; correct source instead of silently modifying it");
        if (trim("Vendor").length() > 190) throw new IllegalArgumentException("Vendor too long");
        String status = trim("Status");
        if (!status.isEmpty() && !List.of("active","draft","archived").contains(status)) {
            throw new IllegalArgumentException("Unsupported product Status");
        }
        optionalBoolean(trim("Published"));
        optionalBoolean(trim("Variant Requires Shipping"));
        optionalBoolean(trim("Variant Taxable"));
        optionalNonnegativeInt(trim("Variant Inventory Qty"), "Inventory quantity");
        optionalNonnegativeInt(trim("Variant Grams"), "Variant Grams");
        optionalNonnegativeInt(trim("Image Position"), "Image position");
        if (!sku().isEmpty()) kwdPrice(currency);
        else if (!trim("Variant Price").isEmpty()) throw new IllegalArgumentException("Variant Price without Variant SKU");
        for (String field : List.of("Image Src", "Variant Image")) {
            if (trim(field).length()>4096) throw new IllegalArgumentException(field + " URL is too long");
        }
        for (int i=1; i<=3; i++) {
            String name = trim("Option" + i + " Name");
            String value = trim("Option" + i + " Value");
            if (!name.isEmpty() && name.length() > 190) throw new IllegalArgumentException("Option name too long");
            if (!value.isEmpty() && value.length() > 255) throw new IllegalArgumentException("Option value too long");
            if (!value.isEmpty() && name.isEmpty()) throw new IllegalArgumentException("Option value without option name");
        }
        for (String field : List.of("Image Src", "Variant Image")) {
            if (!trim(field).isEmpty() && ExternalImageUrlPolicy.isAllowed(trim(field))) {
                URI.create(trim(field));
            }
        }
    }
}
