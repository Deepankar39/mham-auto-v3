-- Execute after Phase 2 sample import using Workbench; queries are READ ONLY.
USE mham_auto;
SELECT 'products' AS entity, COUNT(*) AS total FROM products
UNION ALL SELECT 'variants',COUNT(*) FROM product_variants
UNION ALL SELECT 'media_assets',COUNT(*) FROM media_assets
UNION ALL SELECT 'product_images',COUNT(*) FROM product_images
UNION ALL SELECT 'variant_images',COUNT(*) FROM variant_images
UNION ALL SELECT 'inventory',COUNT(*) FROM inventory
UNION ALL SELECT 'unverified_prices',COUNT(*) FROM product_variants WHERE retail_price_kwd IS NULL;
SELECT p.handle,v.sku_raw,v.source_price_text,v.source_currency,v.retail_price_kwd,
       i.source_quantity,i.verified_live
FROM products p JOIN product_variants v ON v.product_id=p.id
LEFT JOIN inventory i ON i.variant_id=v.id
ORDER BY p.id LIMIT 25;
SELECT p.handle, pi.source_position, m.original_url, pi.source_alt_text
FROM product_images pi JOIN products p ON p.id=pi.product_id
JOIN media_assets m ON m.id=pi.media_id ORDER BY p.id,pi.source_position LIMIT 25;
SELECT id,source_name,status,rows_seen,rows_imported,errors_seen,warnings_seen
FROM import_jobs ORDER BY id DESC LIMIT 12;
