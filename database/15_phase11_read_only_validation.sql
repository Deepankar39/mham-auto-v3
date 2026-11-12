-- MHAM AUTO Phase 11 | READ ONLY (MySQL Workbench). Run against mham_auto test database.
-- No DDL/DML. Does not extract private names/phones/emails or password hashes.
USE mham_auto;
SELECT 'Schema version tracking' AS section, version, applied_at FROM schema_migrations ORDER BY version;
SELECT 'Product count' AS metric, COUNT(*) AS total FROM products;
SELECT 'Variant count' AS metric, COUNT(*) AS total FROM product_variants;
SELECT 'Media asset URLs' AS metric, COUNT(*) AS total FROM media_assets;
SELECT 'Product image relationships' AS metric, COUNT(*) AS total FROM product_images;
SELECT 'Missing product for variants' AS metric, COUNT(*) AS total FROM product_variants v LEFT JOIN products p ON v.product_id=p.id WHERE p.id IS NULL;
SELECT 'Missing media for image associations' AS metric, COUNT(*) AS total FROM product_images i LEFT JOIN media_assets m ON i.media_id=m.id WHERE m.id IS NULL;
SELECT 'Users' AS metric, COUNT(*) AS total FROM users;
SELECT 'Carts' AS metric, COUNT(*) AS total FROM carts;
SELECT 'Demo orders' AS metric, COUNT(*) AS total FROM demo_orders;
SELECT 'Trade applications' AS metric, COUNT(*) AS total FROM business_applications;
SELECT 'Trade events' AS metric, COUNT(*) AS total FROM business_application_events;
SELECT table_name, table_rows FROM information_schema.tables WHERE table_schema='mham_auto' ORDER BY table_name;
-- information_schema.table_rows is a storage-engine estimate; use exact COUNT(*) for acceptance thresholds.
