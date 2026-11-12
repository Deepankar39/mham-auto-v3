-- MHAM AUTO / V001 catalogue; MySQL 8.0+, InnoDB, MySQL 8 collation.
-- The Java migration runner tracks the script checksum and avoids repeat application.
CREATE TABLE IF NOT EXISTS vendors (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(190) NOT NULL,
  UNIQUE KEY uq_vendor_name (name)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS categories (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  parent_id BIGINT NULL,
  name VARCHAR(190) NOT NULL,
  external_taxonomy TEXT NOT NULL,
  taxonomy_sha256 CHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
  CONSTRAINT fk_category_parent FOREIGN KEY (parent_id) REFERENCES categories(id),
  UNIQUE KEY uq_category_source_hash (taxonomy_sha256)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS import_jobs (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  source_name VARCHAR(250) NOT NULL,
  source_sha256 CHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
  source_currency CHAR(3) CHARACTER SET ascii COLLATE ascii_bin NULL,
  status ENUM('RUNNING','COMPLETED','PARTIAL','FAILED') NOT NULL DEFAULT 'RUNNING',
  rows_seen BIGINT NOT NULL DEFAULT 0,
  rows_imported BIGINT NOT NULL DEFAULT 0,
  errors_seen BIGINT NOT NULL DEFAULT 0,
  warnings_seen BIGINT NOT NULL DEFAULT 0,
  started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  finished_at TIMESTAMP NULL,
  KEY ix_import_sha (source_sha256)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS products (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  handle VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  title VARCHAR(512) NOT NULL,
  body_html MEDIUMTEXT NULL,
  vendor_id BIGINT NULL,
  category_id BIGINT NULL,
  source_product_type VARCHAR(160) NULL,
  tags_raw TEXT NULL,
  seo_title VARCHAR(512) NULL,
  seo_description TEXT NULL,
  published BOOLEAN NULL,
  status ENUM('active','draft','archived') NOT NULL DEFAULT 'draft',
  source_job_id BIGINT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_product_handle (handle),
  KEY ix_product_visible (status,published,id),
  KEY ix_product_category (category_id,status,id),
  KEY ix_product_vendor (vendor_id,status,id),
  FULLTEXT KEY ft_product_title (title),
  CONSTRAINT fk_product_vendor FOREIGN KEY (vendor_id) REFERENCES vendors(id),
  CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES categories(id),
  CONSTRAINT fk_product_import FOREIGN KEY (source_job_id) REFERENCES import_jobs(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS product_variants (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  product_id BIGINT NOT NULL,
  sku_raw VARCHAR(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  source_price_text VARCHAR(64) NULL,
  source_currency CHAR(3) CHARACTER SET ascii COLLATE ascii_bin NULL,
  retail_price_kwd DECIMAL(18,3) NULL,
  compare_at_price_text VARCHAR(64) NULL,
  barcode VARCHAR(190) NULL,
  grams INT NULL,
  weight_unit VARCHAR(10) NULL,
  requires_shipping BOOLEAN NULL,
  taxable BOOLEAN NULL,
  inventory_policy VARCHAR(20) NULL,
  source_job_id BIGINT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_variant_sku (sku_raw),
  KEY ix_variant_product (product_id,id),
  CONSTRAINT chk_nonnegative_kwd CHECK (retail_price_kwd IS NULL OR retail_price_kwd >= 0),
  CONSTRAINT fk_variant_product FOREIGN KEY (product_id) REFERENCES products(id),
  CONSTRAINT fk_variant_import FOREIGN KEY (source_job_id) REFERENCES import_jobs(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS product_options (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  product_id BIGINT NOT NULL,
  option_position TINYINT NOT NULL,
  option_name VARCHAR(190) NOT NULL,
  UNIQUE KEY uq_product_option (product_id,option_position),
  CONSTRAINT chk_option_position CHECK (option_position BETWEEN 1 AND 3),
  CONSTRAINT fk_option_product FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS variant_option_values (
  variant_id BIGINT NOT NULL,
  product_option_id BIGINT NOT NULL,
  option_value VARCHAR(255) NOT NULL,
  PRIMARY KEY (variant_id,product_option_id),
  CONSTRAINT fk_vov_variant FOREIGN KEY (variant_id) REFERENCES product_variants(id),
  CONSTRAINT fk_vov_option FOREIGN KEY (product_option_id) REFERENCES product_options(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS inventory (
  variant_id BIGINT NOT NULL PRIMARY KEY,
  source_quantity INT NULL,
  source_tracker VARCHAR(32) NULL,
  verified_live BOOLEAN NOT NULL DEFAULT FALSE,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT chk_nonnegative_qty CHECK (source_quantity IS NULL OR source_quantity >= 0),
  CONSTRAINT fk_inventory_variant FOREIGN KEY (variant_id) REFERENCES product_variants(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS media_assets (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  url_sha256 CHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
  original_url TEXT NOT NULL,
  storage_key VARCHAR(512) NULL,
  delivery_url TEXT NULL,
  source_host VARCHAR(128) NOT NULL DEFAULT 'cdn.shopify.com',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_media_hash (url_sha256)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS product_images (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  product_id BIGINT NOT NULL,
  media_id BIGINT NOT NULL,
  source_position INT NULL,
  source_alt_text VARCHAR(512) NULL,
  alt_text_override VARCHAR(512) NULL,
  UNIQUE KEY uq_product_media (product_id,media_id),
  KEY ix_product_image_order (product_id,source_position,id),
  CONSTRAINT fk_pi_product FOREIGN KEY (product_id) REFERENCES products(id),
  CONSTRAINT fk_pi_media FOREIGN KEY (media_id) REFERENCES media_assets(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS variant_images (
  variant_id BIGINT NOT NULL,
  media_id BIGINT NOT NULL,
  PRIMARY KEY (variant_id,media_id),
  CONSTRAINT fk_vi_variant FOREIGN KEY (variant_id) REFERENCES product_variants(id),
  CONSTRAINT fk_vi_media FOREIGN KEY (media_id) REFERENCES media_assets(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS variant_market_availability (
  variant_id BIGINT NOT NULL,
  market_key VARCHAR(24) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
  included BOOLEAN NULL,
  raw_price VARCHAR(64) NULL,
  raw_compare_price VARCHAR(64) NULL,
  PRIMARY KEY (variant_id,market_key),
  CONSTRAINT fk_market_variant FOREIGN KEY (variant_id) REFERENCES product_variants(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS product_tags (
  product_id BIGINT NOT NULL,
  tag_value VARCHAR(190) NOT NULL,
  PRIMARY KEY (product_id,tag_value),
  CONSTRAINT fk_tag_product FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS import_errors (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  job_id BIGINT NOT NULL,
  record_number BIGINT NOT NULL,
  severity ENUM('WARN','ERROR') NOT NULL,
  message VARCHAR(700) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY ix_import_error_job (job_id,record_number),
  CONSTRAINT fk_ie_job FOREIGN KEY (job_id) REFERENCES import_jobs(id)
) ENGINE=InnoDB;
