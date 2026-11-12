-- READ-ONLY Phase 4 verification: after IdentityCli migrate (V002)
USE mham_auto;
SELECT version,LEFT(sha256,12) AS checksum_prefix,applied_at
FROM schema_migrations WHERE version IN ('V001','V002') ORDER BY version;
SELECT r.role_code,COUNT(ur.user_id) AS assigned_accounts
FROM roles r LEFT JOIN user_roles ur ON ur.role_id=r.id
GROUP BY r.id,r.role_code ORDER BY r.role_code;
SELECT COUNT(*) AS total_accounts,
       SUM(status='ACTIVE') AS active_accounts,
       SUM(status='DISABLED') AS disabled_accounts
FROM users;
SELECT u.id,u.email_normalized,u.status,GROUP_CONCAT(r.role_code ORDER BY r.role_code) AS roles
FROM users u LEFT JOIN user_roles ur ON ur.user_id=u.id
LEFT JOIN roles r ON r.id=ur.role_id
GROUP BY u.id,u.email_normalized,u.status
ORDER BY u.id DESC LIMIT 10;
