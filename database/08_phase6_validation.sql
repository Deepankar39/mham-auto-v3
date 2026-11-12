-- Read-only Phase 6 / V004 validation; run in MySQL Workbench after migration.
USE mham_auto;
SELECT version, applied_at FROM schema_migrations WHERE version IN ('V001','V002','V003','V004') ORDER BY version;
SELECT (SELECT COUNT(*) FROM customer_addresses) AS saved_addresses,
       (SELECT COUNT(*) FROM demo_orders) AS demo_orders,
       (SELECT COUNT(*) FROM demo_order_items) AS demo_order_lines,
       (SELECT COUNT(*) FROM demo_price_assignments) AS explicit_demo_price_audits;
-- Should return ZERO inconsistent rows.
SELECT o.id AS invalid_order_id FROM demo_orders o
LEFT JOIN (SELECT order_id, SUM(line_total_kwd) AS sum_lines FROM demo_order_items GROUP BY order_id) i ON i.order_id=o.id
WHERE o.total_kwd <> o.subtotal_kwd OR o.tax_kwd <> 0 OR o.shipping_kwd <> 0
   OR i.sum_lines IS NULL OR i.sum_lines <> o.subtotal_kwd;
-- Should return ZERO duplicate tokens within any customer.
SELECT user_id, checkout_token, COUNT(*) AS repeated FROM demo_orders GROUP BY user_id,checkout_token HAVING repeated > 1;
-- Snapshot fields are stored on demo_orders/items; deleting/updating saved addresses will not change the snapshot.
