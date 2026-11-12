-- V002 / MHAM AUTO academic identity. Apply using IdentityCli migrate, NOT Phase2 V001 migrator.
-- Append-only migration; no existing catalogue table is modified or dropped.
CREATE TABLE IF NOT EXISTS users (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  email_normalized VARCHAR(254) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  display_name VARCHAR(100) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  status ENUM('ACTIVE','DISABLED') NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_user_email (email_normalized),
  KEY ix_user_status (status,id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS roles (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  role_code VARCHAR(32) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
  UNIQUE KEY uq_roles_code (role_code)
) ENGINE=InnoDB;

INSERT INTO roles (role_code) VALUES ('CUSTOMER') ON DUPLICATE KEY UPDATE role_code=VALUES(role_code);
INSERT INTO roles (role_code) VALUES ('ADMIN') ON DUPLICATE KEY UPDATE role_code=VALUES(role_code);

CREATE TABLE IF NOT EXISTS user_roles (
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (user_id,role_id),
  KEY ix_user_roles_role (role_id,user_id),
  CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id)
) ENGINE=InnoDB;
