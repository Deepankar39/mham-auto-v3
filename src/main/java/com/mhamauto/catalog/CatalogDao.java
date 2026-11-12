package com.mhamauto.catalog;

import com.mhamauto.catalog.CatalogModels.*;
import com.mhamauto.media.ExternalImageUrlPolicy;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/** Read-only, prepared-query catalogue access. No table scans over Java collections. */
public final class CatalogDao {
    private static final String LIVE = "p.status='active' AND p.published=TRUE";
    private static final String VISIBLE_VARIANT = "EXISTS (SELECT 1 FROM product_variants x LEFT JOIN " +
       "variant_market_availability a ON a.variant_id=x.id AND a.market_key='KW' " +
       "WHERE x.product_id=p.id AND (a.included IS NULL OR a.included=TRUE))";

    public Page search(Connection connection, CatalogQuery q) throws SQLException {
        StringBuilder where = new StringBuilder(" WHERE " + LIVE + " AND " + VISIBLE_VARIANT);
        List<Object> params = new ArrayList<>();
        if (q.vendorId() != null) { where.append(" AND p.vendor_id=?"); params.add(q.vendorId()); }
        if (q.uncategorized()) where.append(" AND p.category_id IS NULL");
        else if (q.categoryId() != null) { where.append(" AND p.category_id=?"); params.add(q.categoryId()); }
        if (!q.query().isBlank()) {
            String fullText = q.fullTextTerms();
            where.append(" AND (");
            if (!fullText.isBlank()) {
                where.append("MATCH(p.title) AGAINST (? IN BOOLEAN MODE) OR "); params.add(fullText);
            } else {
                where.append("p.title LIKE ? ESCAPE '!' OR "); params.add(q.skuPrefix());
            }
            where.append("EXISTS (SELECT 1 FROM product_variants s WHERE s.product_id=p.id AND s.sku_raw LIKE ? ESCAPE '!'))");
            params.add(q.skuPrefix());
        }
        long total;
        try (PreparedStatement ps = prep(connection, "SELECT COUNT(*) FROM products p" + where, params);
             ResultSet rs = ps.executeQuery()) { rs.next(); total = rs.getLong(1); }
        String sort = switch (q.sort()) {
            case "title_asc" -> "p.title ASC,p.id ASC";
            case "title_desc" -> "p.title DESC,p.id DESC";
            case "price_asc" -> "(first_price IS NULL) ASC,first_price ASC,p.id ASC";
            case "price_desc" -> "(first_price IS NULL) ASC,first_price DESC,p.id ASC";
            default -> "p.id DESC";
        };
        // A derived table makes price alias sortable across supported MySQL 8 versions.
        String skuSql = "(SELECT x.sku_raw FROM product_variants x LEFT JOIN variant_market_availability ma " +
          "ON ma.variant_id=x.id AND ma.market_key='KW' WHERE x.product_id=p.id " +
          "AND (ma.included IS NULL OR ma.included=TRUE) ORDER BY x.id LIMIT 1)";
        String priceSql = "(SELECT MIN(x.retail_price_kwd) FROM product_variants x LEFT JOIN variant_market_availability ma " +
          "ON ma.variant_id=x.id AND ma.market_key='KW' WHERE x.product_id=p.id AND (ma.included IS NULL OR ma.included=TRUE))";
        String imageSql = "(SELECT m.original_url FROM product_images i JOIN media_assets m ON m.id=i.media_id " +
          "WHERE i.product_id=p.id ORDER BY i.source_position IS NULL,i.source_position,i.id LIMIT 1)";
        String inner = "SELECT p.id,p.handle,p.title,COALESCE(v.name,'Not specified') vendor," +
          "COALESCE(c.name,'Uncategorised') category," + skuSql + " sku," + priceSql + " first_price," + imageSql + " image_url" +
          " FROM products p LEFT JOIN vendors v ON p.vendor_id=v.id LEFT JOIN categories c ON p.category_id=c.id" + where;
        String sql = "SELECT * FROM (" + inner + ") p ORDER BY " + sort.replace("p.title", "p.title").replace("p.id", "p.id") + " LIMIT ? OFFSET ?";
        List<Object> pageArgs = new ArrayList<>(params); pageArgs.add(CatalogQuery.PAGE_SIZE); pageArgs.add(q.offset());
        List<Card> items = new ArrayList<>();
        try (PreparedStatement ps = prep(connection, sql, pageArgs); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) items.add(new Card(rs.getLong("id"),rs.getString("handle"),rs.getString("title"),
                    rs.getString("vendor"),rs.getString("category"),rs.getString("sku"),rs.getBigDecimal("first_price"),
                    rs.getString("image_url")));
        }
        return new Page(List.copyOf(items), total, q.page(), CatalogQuery.PAGE_SIZE);
    }

    public List<Facet> vendors(Connection connection) throws SQLException {
        String sql = "SELECT v.id,v.name FROM vendors v WHERE EXISTS (SELECT 1 FROM products p " +
            "WHERE p.vendor_id=v.id AND " + LIVE + " AND " + VISIBLE_VARIANT + ") ORDER BY v.name,v.id LIMIT 200";
        return facets(connection, sql);
    }
    public List<Facet> categories(Connection connection) throws SQLException {
        String sql = "SELECT c.id,c.name FROM categories c WHERE EXISTS (SELECT 1 FROM products p " +
            "WHERE p.category_id=c.id AND " + LIVE + " AND " + VISIBLE_VARIANT + ") ORDER BY c.name,c.id LIMIT 200";
        return facets(connection, sql);
    }
    private List<Facet> facets(Connection connection,String sql) throws SQLException {
        List<Facet> facets = new ArrayList<>();
        try (PreparedStatement ps=connection.prepareStatement(sql); ResultSet r=ps.executeQuery()) {
            while (r.next()) facets.add(new Facet(r.getLong(1),r.getString(2)));
        }
        return List.copyOf(facets);
    }

    /** Returns null when missing, unpublished, or hidden from the Kuwait market. */
    public Detail detail(Connection connection, String handle) throws SQLException {
        String sql = "SELECT p.id,p.handle,p.title,p.body_html,COALESCE(v.name,'Not specified') vendor," +
            "COALESCE(c.name,'Uncategorised') category FROM products p " +
            "LEFT JOIN vendors v ON p.vendor_id=v.id LEFT JOIN categories c ON p.category_id=c.id " +
            "WHERE p.handle=? AND " + LIVE + " AND " + VISIBLE_VARIANT;
        long id; String title, vendor, category, body, originalHandle;
        try (PreparedStatement ps=connection.prepareStatement(sql)) {
            ps.setString(1,handle);
            try (ResultSet r=ps.executeQuery()) {
                if (!r.next()) return null;
                id=r.getLong("id"); title=r.getString("title"); originalHandle=r.getString("handle");
                vendor=r.getString("vendor"); category=r.getString("category"); body=r.getString("body_html");
            }
        }
        List<Image> images = new ArrayList<>();
        try (PreparedStatement ps=connection.prepareStatement("SELECT m.original_url,i.alt_text_override," +
            "i.source_alt_text FROM product_images i JOIN media_assets m ON m.id=i.media_id " +
            "WHERE i.product_id=? ORDER BY i.source_position IS NULL,i.source_position,i.id LIMIT 24")) {
            ps.setLong(1,id);
            try (ResultSet r=ps.executeQuery()) {
                while (r.next()) {
                    String url=r.getString(1), alt=r.getString(2);
                    // Source sample has provably mismatched MINI Cooper alt text. Don't render it.
                    if (alt==null || alt.isBlank()) alt=title;
                    if (ExternalImageUrlPolicy.isAllowed(url)) images.add(new Image(url,alt));
                }
            }
        }
        List<Variant> variants = new ArrayList<>();
        try (PreparedStatement ps=connection.prepareStatement("SELECT x.id,x.sku_raw,x.retail_price_kwd," +
          "i.source_quantity,i.verified_live FROM product_variants x " +
          "LEFT JOIN inventory i ON i.variant_id=x.id LEFT JOIN variant_market_availability ma " +
          "ON ma.variant_id=x.id AND ma.market_key='KW' WHERE x.product_id=? AND " +
          "(ma.included IS NULL OR ma.included=TRUE) ORDER BY x.id LIMIT 100")) {
            ps.setLong(1,id);
            try (ResultSet r=ps.executeQuery()) {
                while (r.next()) {
                    int qty=r.getInt("source_quantity"); Integer amount=r.wasNull()?null:qty;
                    long vid=r.getLong("id");
                    variants.add(new Variant(vid,r.getString("sku_raw"),r.getBigDecimal("retail_price_kwd"),
                        amount,r.getBoolean("verified_live"),variantOptions(connection,vid)));
                }
            }
        }
        return new Detail(id,originalHandle,title,vendor,category,DescriptionText.excerpt(body),
            List.copyOf(images),List.copyOf(variants));
    }
    private String variantOptions(Connection connection,long variantId) throws SQLException {
        StringJoiner values=new StringJoiner(" · ");
        try (PreparedStatement ps=connection.prepareStatement("SELECT o.option_name,v.option_value FROM variant_option_values v " +
          "JOIN product_options o ON o.id=v.product_option_id WHERE v.variant_id=? ORDER BY o.option_position")) {
            ps.setLong(1,variantId);
            try (ResultSet r=ps.executeQuery()) {
                while (r.next()) values.add(r.getString(1) + ": " + r.getString(2));
            }
        }
        return values.toString();
    }
    private static PreparedStatement prep(Connection c,String sql,List<Object> values) throws SQLException {
        PreparedStatement p=c.prepareStatement(sql);
        // Prevent unbounded catalogue queries from tying up the small Academic V1 pool.
        // This is a safety bound, not evidence of million-product performance.
        p.setQueryTimeout(10);
        for(int i=0;i<values.size();i++) p.setObject(i+1, values.get(i));
        return p;
    }
}
