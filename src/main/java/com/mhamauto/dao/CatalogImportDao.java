package com.mhamauto.dao;

import com.mhamauto.importer.ShopifyRecord;
import com.mhamauto.media.ExternalImageUrlPolicy;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/** JDBC importer. Uses parameterized SQL and stable IDs; no HTTP requests or media BLOBs. */
public final class CatalogImportDao implements AutoCloseable {
    private final Connection connection;
    private final Map<String,PreparedStatement> cache = new HashMap<>();
    private final long jobId;
    private final String sourceCurrency;

    public CatalogImportDao(Connection connection, long jobId, String sourceCurrency) {
        this.connection = connection;
        this.jobId = jobId;
        this.sourceCurrency = sourceCurrency;
    }

    private PreparedStatement prepare(String sql, Object... values) throws SQLException {
        PreparedStatement ps = cache.get(sql);
        if (ps == null) {
            ps = connection.prepareStatement(sql);
            cache.put(sql, ps);
        }
        ps.clearParameters();
        for (int i=0; i<values.length; i++) ps.setObject(i+1, values[i]);
        return ps;
    }
    private void update(String sql, Object... values) throws SQLException { prepare(sql, values).executeUpdate(); }
    private Long selectLong(String sql, Object... values) throws SQLException {
        try (ResultSet r = prepare(sql, values).executeQuery()) { return r.next() ? r.getLong(1) : null; }
    }
    private long lastId() throws SQLException {
        Long id = selectLong("SELECT LAST_INSERT_ID()");
        if (id == null || id == 0) throw new SQLException("Could not retrieve insert ID");
        return id;
    }
    private Long vendor(String name) throws SQLException {
        if (name.isBlank()) return null;
        update("INSERT INTO vendors(name) VALUES (?) ON DUPLICATE KEY UPDATE id=LAST_INSERT_ID(id)", name);
        return lastId();
    }
    private Long category(String name) throws SQLException {
        if (name.isBlank()) return null;
        String display = name.length() > 190 ? name.substring(0,190) : name;
        if (name.length() > 512) throw new IllegalArgumentException("Product Category too long");
        update("INSERT INTO categories(name,external_taxonomy,taxonomy_sha256) VALUES (?,?,?) " +
               "ON DUPLICATE KEY UPDATE id=LAST_INSERT_ID(id)", display,name,sha256(name));
        return lastId();
    }

    public void accept(ShopifyRecord r) throws SQLException {
        r.validate(sourceCurrency);
        Long productId = selectLong("SELECT id FROM products WHERE handle=?", r.handle());
        if (!r.title().isEmpty()) {
            Long vendorId = vendor(r.trim("Vendor"));
            Long categoryId = category(r.trim("Product Category"));
            update("""
                INSERT INTO products(handle,title,body_html,vendor_id,category_id,source_product_type,
                    tags_raw,seo_title,seo_description,published,status,source_job_id)
                VALUES (?,?,?,?,?,?,?,?,?,?,?,?)
                ON DUPLICATE KEY UPDATE id=LAST_INSERT_ID(id),title=VALUES(title),
                    body_html=COALESCE(VALUES(body_html),body_html),
                    vendor_id=COALESCE(VALUES(vendor_id),vendor_id),
                    category_id=COALESCE(VALUES(category_id),category_id),
                    source_product_type=COALESCE(VALUES(source_product_type),source_product_type),
                    tags_raw=COALESCE(VALUES(tags_raw),tags_raw),
                    seo_title=COALESCE(VALUES(seo_title),seo_title),
                    seo_description=COALESCE(VALUES(seo_description),seo_description),
                    published=COALESCE(VALUES(published),published),
                    status=VALUES(status),source_job_id=VALUES(source_job_id)
                """,r.handle(),r.title(),nullable(r.field("Body (HTML)")),vendorId,categoryId,
                nullable(r.trim("Type")),nullable(r.trim("Tags")),nullable(r.trim("SEO Title")),
                nullable(r.trim("SEO Description")),ShopifyRecord.optionalBoolean(r.trim("Published")),
                r.trim("Status").isEmpty()?"draft":r.trim("Status"),jobId);
            productId = lastId();
            String tags = r.trim("Tags");
            if (!tags.isBlank()) {
                for (String tag : tags.split(",")) {
                    tag = tag.trim();
                    if (tag.isEmpty()) continue;
                    if (tag.length() > 190) throw new IllegalArgumentException("Tag longer than 190 characters");
                    update("INSERT IGNORE INTO product_tags(product_id,tag_value) VALUES (?,?)",productId,tag);
                }
            }
        }
        if (productId == null) throw new IllegalArgumentException("Handle has no base product yet; export rows must begin with product data");

        Long variantId = null;
        if (!r.sku().isBlank()) {
            Long existingProduct = selectLong("SELECT product_id FROM product_variants WHERE sku_raw=?",r.sku());
            if (existingProduct != null && existingProduct.longValue() != productId.longValue()) {
                throw new IllegalArgumentException("SKU already belongs to another product handle");
            }
            update("""
                INSERT INTO product_variants(product_id,sku_raw,source_price_text,source_currency,
                   retail_price_kwd,compare_at_price_text,barcode,grams,weight_unit,requires_shipping,
                   taxable,inventory_policy,source_job_id)
                VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)
                ON DUPLICATE KEY UPDATE id=LAST_INSERT_ID(id),
                    source_price_text=COALESCE(VALUES(source_price_text),source_price_text),
                    source_currency=IF(VALUES(source_price_text) IS NOT NULL,VALUES(source_currency),source_currency),
                    retail_price_kwd=IF(VALUES(source_price_text) IS NOT NULL,VALUES(retail_price_kwd),retail_price_kwd),
                    compare_at_price_text=COALESCE(VALUES(compare_at_price_text),compare_at_price_text),
                    barcode=COALESCE(VALUES(barcode),barcode),
                    grams=COALESCE(VALUES(grams),grams),
                    weight_unit=COALESCE(VALUES(weight_unit),weight_unit),
                    requires_shipping=COALESCE(VALUES(requires_shipping),requires_shipping),
                    taxable=COALESCE(VALUES(taxable),taxable),
                    inventory_policy=COALESCE(VALUES(inventory_policy),inventory_policy),
                    source_job_id=VALUES(source_job_id)
                """,productId,r.sku(),nullable(r.trim("Variant Price")),
                sourceCurrency.equals("KWD")?"KWD":null,r.kwdPrice(sourceCurrency),
                nullable(r.trim("Variant Compare At Price")),nullable(r.trim("Variant Barcode")),
                ShopifyRecord.optionalNonnegativeInt(r.trim("Variant Grams"),"Grams"),
                nullable(r.trim("Variant Weight Unit")),ShopifyRecord.optionalBoolean(r.trim("Variant Requires Shipping")),
                ShopifyRecord.optionalBoolean(r.trim("Variant Taxable")),nullable(r.trim("Variant Inventory Policy")),jobId);
            variantId = lastId();
            for (int pos=1; pos<=3; pos++) {
                String name = r.trim("Option" + pos + " Name");
                String value = r.trim("Option" + pos + " Value");
                if (name.isEmpty()) continue;
                Long optionId = selectLong("SELECT id FROM product_options WHERE product_id=? AND option_position=?",productId,pos);
                if (optionId == null) {
                    update("INSERT INTO product_options(product_id,option_position,option_name) VALUES (?,?,?)",productId,pos,name);
                    optionId=lastId();
                } else {
                    String existingName = selectString("SELECT option_name FROM product_options WHERE id=?",optionId);
                    if (!name.equals(existingName)) throw new IllegalArgumentException("Conflicting option name at position " + pos);
                }
                if (!value.isEmpty()) {
                    update("INSERT INTO variant_option_values(variant_id,product_option_id,option_value) VALUES (?,?,?) " +
                           "ON DUPLICATE KEY UPDATE option_value=VALUES(option_value)",variantId,optionId,value);
                }
            }
            Integer qty = ShopifyRecord.optionalNonnegativeInt(r.trim("Variant Inventory Qty"),"Inventory quantity");
            if (qty != null || !r.trim("Variant Inventory Tracker").isEmpty()) {
                update("INSERT INTO inventory(variant_id,source_quantity,source_tracker,verified_live) VALUES (?,?,?,FALSE) " +
                       "ON DUPLICATE KEY UPDATE source_quantity=COALESCE(VALUES(source_quantity),source_quantity)," +
                       "source_tracker=COALESCE(VALUES(source_tracker),source_tracker),verified_live=FALSE",
                        variantId,qty,nullable(r.trim("Variant Inventory Tracker")));
            }
            market(variantId,"KW",r,"Kuwait");
            market(variantId,"INT",r,"International");
            market(variantId,"SA",r,"Saudi Arabia");
        }
        String image = r.trim("Image Src");
        if (!image.isBlank() && ExternalImageUrlPolicy.isAllowed(image)) {
            long media = mediaId(image);
            update("INSERT INTO product_images(product_id,media_id,source_position,source_alt_text) VALUES (?,?,?,?) " +
                   "ON DUPLICATE KEY UPDATE source_position=COALESCE(VALUES(source_position),source_position)," +
                   "source_alt_text=COALESCE(VALUES(source_alt_text),source_alt_text)",productId,media,
                   ShopifyRecord.optionalNonnegativeInt(r.trim("Image Position"),"Image Position"),nullable(r.trim("Image Alt Text")));
        }
        String variantImage = r.trim("Variant Image");
        if (!variantImage.isBlank() && ExternalImageUrlPolicy.isAllowed(variantImage)) {
            long media = mediaId(variantImage);
            if (variantId != null) {
                update("INSERT IGNORE INTO variant_images(variant_id,media_id) VALUES (?,?)",variantId,media);
            }
            // On an image-only row the variant association is unknown; keep product media anyway.
            update("INSERT IGNORE INTO product_images(product_id,media_id) VALUES (?,?)",productId,media);
        }
    }

    private void market(Long id, String code, ShopifyRecord r, String fieldSuffix) throws SQLException {
        Boolean included = ShopifyRecord.optionalBoolean(r.trim("Included / " + fieldSuffix));
        String rawPrice = nullable(r.trim("Price / " + fieldSuffix));
        String rawCompare = nullable(r.trim("Compare At Price / " + fieldSuffix));
        if (included == null && rawPrice == null && rawCompare == null) return;
        update("INSERT INTO variant_market_availability(variant_id,market_key,included,raw_price,raw_compare_price) " +
              "VALUES (?,?,?,?,?) ON DUPLICATE KEY UPDATE included=COALESCE(VALUES(included),included)," +
              "raw_price=COALESCE(VALUES(raw_price),raw_price),raw_compare_price=COALESCE(VALUES(raw_compare_price),raw_compare_price)",
              id,code,included,rawPrice,rawCompare);
    }
    private String selectString(String sql, Object... args) throws SQLException {
        try (ResultSet r=prepare(sql,args).executeQuery()) { return r.next()?r.getString(1):null; }
    }
    private long mediaId(String url) throws SQLException {
        String hash = sha256(url);
        update("INSERT INTO media_assets(url_sha256,original_url) VALUES (?,?) ON DUPLICATE KEY UPDATE id=LAST_INSERT_ID(id)",hash,url);
        long id=lastId();
        String old=selectString("SELECT original_url FROM media_assets WHERE id=?",id);
        if (!url.equals(old)) throw new IllegalArgumentException("URL hash collision or conflicting media source");
        return id;
    }
    private static String nullable(String s) { return s == null || s.isBlank() ? null : s; }
    private static String sha256(String text) {
        try { return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8))); }
        catch (NoSuchAlgorithmException impossible) { throw new IllegalStateException(impossible); }
    }
    public void issue(long recordNumber, String severity, String message) throws SQLException {
        update("INSERT INTO import_errors(job_id,record_number,severity,message) VALUES (?,?,?,?)",jobId,recordNumber,severity,
            message.length()>700?message.substring(0,700):message);
    }
    @Override public void close() throws SQLException {
        SQLException error=null;
        for (PreparedStatement ps : cache.values()) try { ps.close(); } catch(SQLException x) { error=x; }
        cache.clear();
        if (error!=null) throw error;
    }
}
