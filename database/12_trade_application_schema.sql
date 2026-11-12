-- V006 Academic V1 simple B2B application only. NO wholesale role, pricing, credit or RFQ.
-- Additive migration; back up the database before applying (MySQL DDL auto-commits).
CREATE TABLE IF NOT EXISTS business_applications (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  company_name VARCHAR(160) NOT NULL,
  contact_name VARCHAR(100) NOT NULL,
  contact_phone VARCHAR(16) NOT NULL,
  business_type VARCHAR(24) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
  governorate VARCHAR(32) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
  business_area VARCHAR(90) NOT NULL,
  applicant_note VARCHAR(500) NOT NULL DEFAULT '',
  status ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING',
  reviewed_by BIGINT NULL,
  reviewed_at TIMESTAMP NULL,
  admin_note VARCHAR(500) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_trade_application_user (user_id),
  KEY ix_trade_status_created (status,created_at,id),
  CONSTRAINT fk_trade_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
  CONSTRAINT fk_trade_reviewer FOREIGN KEY (reviewed_by) REFERENCES users(id) ON DELETE RESTRICT,
  CONSTRAINT chk_trade_business_type CHECK (business_type IN ('GARAGE','WORKSHOP','PARTS_RETAILER','FLEET','OTHER')),
  CONSTRAINT chk_trade_governorate CHECK (governorate IN ('CAPITAL','HAWALLI','FARWANIYA','AHMADI','JAHRA','MUBARAK_AL_KABEER')),
  CONSTRAINT chk_trade_review_state CHECK ((status='PENDING' AND reviewed_by IS NULL AND reviewed_at IS NULL) OR (status IN ('APPROVED','REJECTED') AND reviewed_by IS NOT NULL AND reviewed_at IS NOT NULL))
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS business_application_events (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  application_id BIGINT NOT NULL,
  actor_user_id BIGINT NOT NULL,
  event_type ENUM('SUBMITTED','APPROVED','REJECTED') NOT NULL,
  from_status VARCHAR(12) NULL,
  to_status VARCHAR(12) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY ix_trade_event_application (application_id,id),
  KEY ix_trade_event_actor (actor_user_id,id),
  CONSTRAINT fk_trade_event_application FOREIGN KEY (application_id) REFERENCES business_applications(id) ON DELETE RESTRICT,
  CONSTRAINT fk_trade_event_actor FOREIGN KEY (actor_user_id) REFERENCES users(id) ON DELETE RESTRICT
) ENGINE=InnoDB;
