-- MHAM AUTO Phase 7 — read-only validation; compatible with V004 schema.
-- Execute in MySQL Workbench against the existing local mham_auto database.
USE mham_auto;
SELECT 'user_accounts' AS metric, COUNT(*) AS value FROM users
UNION ALL SELECT 'saved_addresses', COUNT(*) FROM customer_addresses
UNION ALL SELECT 'demo_orders', COUNT(*) FROM demo_orders
UNION ALL SELECT 'demo_order_snapshot_lines', COUNT(*) FROM demo_order_items;

-- Check that the existing Phase 6 owner-first order index is present.
SHOW INDEX FROM demo_orders WHERE Key_name='ix_demo_user_created';

-- Verify demo order totals and immutable line-price aggregates.
SELECT o.id, o.order_reference, o.status, o.total_kwd,
       COALESCE(SUM(oi.line_total_kwd),0.000) AS line_sum_kwd
FROM demo_orders o LEFT JOIN demo_order_items oi ON oi.order_id=o.id
GROUP BY o.id,o.order_reference,o.status,o.total_kwd
HAVING o.total_kwd <> line_sum_kwd
LIMIT 20;
-- Last query should return ZERO rows, assuming demo fees/taxes remain zero as designed.
