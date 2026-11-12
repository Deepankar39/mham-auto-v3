# Database schema reference

MHAM AUTO | Academic V1 | Phase 11 | Release candidate RC1 | 28 October 2026

MySQL 8 / InnoDB. The application schema contains **26 application tables**, plus `schema_migrations` created by Java migration tracking. User-owned application data is stored in MySQL; imported product images are references to external URLs, not BLOBs.

### Versioned migration order
| Version | SQL file | Tables |
|---|---|---|
| V001 | `database/01_catalog_schema.sql` | 14 catalogue/import tables |
| V002 | `database/03_identity_schema.sql` | users, roles, user_roles |
| V003 | `database/05_cart_schema.sql` | carts, cart_items |
| V004 | `database/07_checkout_schema.sql` | customer_addresses, demo_orders, demo_order_items, demo_price_assignments |
| V005 | `database/10_admin_schema.sql` | admin_events |
| V006 | `database/12_trade_application_schema.sql` | business_applications, business_application_events |

### Domain model and data invariants
- Product (`products`) may own variants, images, options and tags; unique handle and variant SKU indexes.
- Media is deduplicated by URL checksum in `media_assets`; `product_images` and `variant_images` join media to records.
- Inventory stores imported quantity with a boolean `verified_live`; avoid treating demo stock as actual available stock.
- Account/role tables are distinct from business applications; approved application != ADMIN privilege.
- Carts and addresses belong to a single user. Checkout copies price, SKU, name and address into immutable demo order snapshots.
- Business application events and admin events create a basic audit trail for demo reviews.

### Schema verification procedure
1. Run migration CLI status for all six phases; validate checksum migrations when retried.
2. Open `database/15_phase11_read_only_validation.sql` in MySQL Workbench and run against a *known test DB*.
3. Capture row counts, FK checks and required-table existence. Do not use screenshot outputs containing personal user rows.
4. Confirm that repeated CSV imports do not duplicate products/variants and that asset/media counts reflect shared image URLs.
5. Do not truncate, delete or alter any database as part of Phase 11 validation.

### Import integrity and source contract
`product data sample.csv` is the source-format contract (74 columns, four sample data rows). Import groups by Shopify Handle and correctly maps variant, option and media rows; originals are retained for traceability. `samples/SYNTHETIC_TEST_500.csv` is only a load-test fixture and deliberately reuses the original image URLs.

### Future-scale strategy
BIGINT IDs, normalized image references, indexed handle/SKU, selective filters and streaming CSV import avoid obvious early bottlenecks. MySQL FULLTEXT can support basic keyword retrieval. Actual 1M+ performance, keyword relevance and operational resilience require new hardware-specific load tests and may eventually justify separate search, CDN-managed assets or different indexing. None are proven by 500-product tests.
