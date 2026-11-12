-- READ ONLY. Execute in MySQL Workbench after TradeCli migrate.
SELECT version,applied_at FROM schema_migrations WHERE version='V006';
SELECT COUNT(*) AS applications FROM business_applications;
SELECT status,COUNT(*) AS total FROM business_applications GROUP BY status;
SELECT COUNT(*) AS review_history FROM business_application_events;
SELECT COUNT(*) AS applications_with_unknown_owner FROM business_applications a LEFT JOIN users u ON u.id=a.user_id WHERE u.id IS NULL;
-- No extra user_roles or wholesale pricing columns are created by Phase 9.
