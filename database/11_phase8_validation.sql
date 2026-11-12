-- Run read-only in MySQL Workbench after AdminCli migrate.
USE mham_auto;
SELECT version,applied_at FROM schema_migrations WHERE version IN ('V004','V005') ORDER BY version;
SELECT (SELECT COUNT(*) FROM products) AS products,
       (SELECT COUNT(*) FROM product_variants) AS variants,
       (SELECT COUNT(*) FROM categories) AS categories,
       (SELECT COUNT(*) FROM demo_orders) AS demo_orders,
       (SELECT COUNT(*) FROM admin_events) AS admin_events;
SELECT entity_type,action,COUNT(*) AS events FROM admin_events GROUP BY entity_type,action ORDER BY entity_type,action;
SELECT id,order_reference,status,created_at FROM demo_orders ORDER BY id DESC LIMIT 10;
