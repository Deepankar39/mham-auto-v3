-- Run against mham_auto with MySQL Workbench after V003 migration.
SELECT version, applied_at FROM schema_migrations WHERE version IN ('V001','V002','V003') ORDER BY version;
SHOW CREATE TABLE carts;
SHOW CREATE TABLE cart_items;
SELECT COUNT(*) AS customer_carts FROM carts;
SELECT COUNT(*) AS cart_lines FROM cart_items;
-- No sample orders, external calls, or production payments are created in Phase 5.
