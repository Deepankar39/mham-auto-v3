-- V005 Phase 8 additive audit log: no modification of prior catalogue, auth, cart or order snapshots.
CREATE TABLE IF NOT EXISTS admin_events (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  actor_user_id BIGINT NOT NULL,
  action VARCHAR(48) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
  entity_type VARCHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
  entity_id BIGINT NOT NULL,
  details VARCHAR(300) NOT NULL DEFAULT '',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY ix_admin_events_time (created_at,id),
  KEY ix_admin_events_target (entity_type,entity_id,created_at),
  CONSTRAINT fk_admin_event_actor FOREIGN KEY (actor_user_id) REFERENCES users(id) ON DELETE RESTRICT
) ENGINE=InnoDB;
