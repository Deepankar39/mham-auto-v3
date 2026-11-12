-- Phase 10: read-only MySQL EXPLAIN / index review. No schema/data changes.
USE mham_auto;
SHOW INDEX FROM products;
SHOW INDEX FROM product_variants;
SHOW INDEX FROM product_images;
SHOW INDEX FROM demo_orders;
EXPLAIN SELECT id,handle,title FROM products
 WHERE status='active' AND published=TRUE ORDER BY id DESC LIMIT 24;
EXPLAIN SELECT id,sku_raw FROM product_variants
 WHERE sku_raw LIKE 'V30%' ORDER BY id LIMIT 24;
EXPLAIN SELECT id,handle,title FROM products
 WHERE category_id IS NULL AND status='active' AND published=TRUE ORDER BY id DESC LIMIT 24;
-- Compare index access, rows examined, latency, and server characteristics before making scaling claims.
