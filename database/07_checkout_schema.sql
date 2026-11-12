-- V004 | MHAM AUTO Academic V1 Phase 6. Append-only, no payments, no stock deduction.
-- Run only through CheckoutCli migrate. MySQL DDL auto-commits; back up database first.
CREATE TABLE IF NOT EXISTS customer_addresses (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  recipient_name VARCHAR(100) NOT NULL,
  phone VARCHAR(16) NOT NULL,
  governorate VARCHAR(32) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
  area VARCHAR(90) NOT NULL,
  block_name VARCHAR(20) NOT NULL,
  street VARCHAR(120) NOT NULL,
  building VARCHAR(60) NOT NULL,
  floor_detail VARCHAR(100) NULL,
  extra_directions VARCHAR(250) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY ix_addresses_user (user_id,id),
  CONSTRAINT fk_address_customer FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS demo_orders (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  order_reference VARCHAR(40) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
  checkout_token CHAR(43) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
  status ENUM('DEMO_PLACED','DEMO_REVIEWED','DEMO_CANCELLED') NOT NULL DEFAULT 'DEMO_PLACED',
  currency CHAR(3) CHARACTER SET ascii COLLATE ascii_bin NOT NULL DEFAULT 'KWD',
  recipient_name VARCHAR(100) NOT NULL,
  phone VARCHAR(16) NOT NULL,
  governorate VARCHAR(32) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
  area VARCHAR(90) NOT NULL,
  block_name VARCHAR(20) NOT NULL,
  street VARCHAR(120) NOT NULL,
  building VARCHAR(60) NOT NULL,
  floor_detail VARCHAR(100) NULL,
  extra_directions VARCHAR(250) NULL,
  subtotal_kwd DECIMAL(18,3) NOT NULL,
  shipping_kwd DECIMAL(18,3) NOT NULL DEFAULT 0.000,
  tax_kwd DECIMAL(18,3) NOT NULL DEFAULT 0.000,
  total_kwd DECIMAL(18,3) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_demo_reference (order_reference),
  UNIQUE KEY uq_demo_user_token (user_id,checkout_token),
  KEY ix_demo_user_created (user_id,created_at,id),
  CONSTRAINT chk_demo_subtotal CHECK (subtotal_kwd >= 0 AND total_kwd >= 0),
  CONSTRAINT chk_demo_no_fees CHECK (shipping_kwd=0 AND tax_kwd=0),
  CONSTRAINT fk_demo_order_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS demo_order_items (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  order_id BIGINT NOT NULL,
  variant_id BIGINT NULL,
  sku_snapshot VARCHAR(191) NOT NULL,
  title_snapshot VARCHAR(512) NOT NULL,
  quantity INT NOT NULL,
  unit_price_kwd DECIMAL(18,3) NOT NULL,
  price_basis ENUM('KWD_CATALOGUE','SYNTHETIC_DEMO') NOT NULL DEFAULT 'KWD_CATALOGUE',
  line_total_kwd DECIMAL(18,3) NOT NULL,
  UNIQUE KEY uq_demo_line_variant (order_id,variant_id),
  CONSTRAINT chk_demo_item_qty CHECK (quantity BETWEEN 1 AND 20),
  CONSTRAINT chk_demo_price_nonnegative CHECK (unit_price_kwd >= 0 AND line_total_kwd >= 0),
  CONSTRAINT fk_demo_line_order FOREIGN KEY (order_id) REFERENCES demo_orders(id) ON DELETE CASCADE,
  CONSTRAINT fk_demo_line_variant FOREIGN KEY (variant_id) REFERENCES product_variants(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- Explicit, reversible fictional prices for imported synthetic test products ONLY.
CREATE TABLE IF NOT EXISTS demo_price_assignments (
  variant_id BIGINT NOT NULL PRIMARY KEY,
  assigned_price_kwd DECIMAL(18,3) NOT NULL,
  source_note VARCHAR(100) NOT NULL DEFAULT 'ACADEMIC_SYNTHETIC_DEMO',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT chk_demo_assignment_price CHECK (assigned_price_kwd >= 0),
  CONSTRAINT fk_demo_price_variant FOREIGN KEY (variant_id) REFERENCES product_variants(id) ON DELETE RESTRICT
) ENGINE=InnoDB;
