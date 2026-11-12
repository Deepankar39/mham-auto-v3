# MHAM AUTO — Shopify CSV source contract (planning notes)

User-provided sample: 74-column Shopify-style export, four sample product rows. These are not yet 500 products.

## Keep source row semantics
- `Handle` groups a product across **multiple possible rows**. Do not assume one row is a product.
- `Variant SKU`, options (1–3), variant prices/quantity represent sellable variants.
- `Image Src`, `Image Position`, `Image Alt Text` represent ordered product media; `Variant Image` is an optional variant-to-image association.
- Duplicate image URLs may be shared and should not be blindly downloaded multiple times.
- `Product Category` can be empty; `Type=Original` is not a valid inferred part-category mapping.
- Some raw SKUs begin with an apostrophe; preserve original text and only normalize after an explicit source rule.
- `Variant Price` has no authoritative currency in this CSV: **do not assume KWD** until confirmed. The target storefront currency is KWD.
- Stock quantity 500 in each sample row is unverified demonstration data, not live availability.
- Descriptions contain HTML, vehicle phrases and some mojibake; sanitize before rendering and separate verified fitment from text.
- Image alt text is repeated incorrectly in the sample; preserve source but correct display alternatives after validation.

## Phase 2 preliminary normalized schema
`products`, `product_variants`, `categories`, `product_options`, `variant_option_values`, `media_assets`, `product_images`, `variant_images`, `inventory`, `import_jobs`, `import_errors`. Identity/cart/orders are added in their roadmap phases. Use InnoDB, BIGINT IDs, controlled unique source keys and DECIMAL(…,3) for KWD values.

Media must be stored as URL/reference metadata; do **not** store downloaded image files in MySQL. Phase 1.4 adds only a no-download CDN URL policy. Original URLs and a future optional object-storage key allow later migration when rights are confirmed.

## Scale contract
Stream and validate CSV input, group same-handle rows, batch idempotent transactional changes, index SKU/handle/category, paginate results. Functional target ~500 products; synthetic performance checkpoints at 50k and 1M+. Actual throughput and latency must be benchmarked; 1M+ support is an architecture target, not a current test result.
