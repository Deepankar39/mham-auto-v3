# Shopify CSV — 74-column source contract

The **input CSV header sequence is authoritative**. No columns are silently reordered or renamed. Only selected fields map to normalized V1 tables; others are preserved by archiving the original file and SHA-256 audit in import_jobs. **Not all 74 fields are copied into MySQL**.

| # | Source CSV column | Phase 2 disposition |
|---:|---|---|
| 1 | `Handle` | products.handle |
| 2 | `Title` | products.title |
| 3 | `Body (HTML)` | products.body_html (sanitize before display) |
| 4 | `Vendor` | vendors.name |
| 5 | `Product Category` | categories.external_taxonomy (if present) |
| 6 | `Type` | products.source_product_type |
| 7 | `Tags` | products.tags_raw; product_tags |
| 8 | `Published` | products.published |
| 9 | `Option1 Name` | product_options.option_name |
| 10 | `Option1 Value` | variant_option_values.option_value |
| 11 | `Option1 Linked To` | Archived source only (future linked options) |
| 12 | `Option2 Name` | product_options.option_name |
| 13 | `Option2 Value` | variant_option_values.option_value |
| 14 | `Option2 Linked To` | Archived source only (future linked options) |
| 15 | `Option3 Name` | product_options.option_name |
| 16 | `Option3 Value` | variant_option_values.option_value |
| 17 | `Option3 Linked To` | Archived source only (future linked options) |
| 18 | `Variant SKU` | product_variants.sku_raw (verbatim) |
| 19 | `Variant Grams` | product_variants.grams |
| 20 | `Variant Inventory Tracker` | inventory.source_tracker |
| 21 | `Variant Inventory Qty` | inventory.source_quantity (unverified) |
| 22 | `Variant Inventory Policy` | product_variants.inventory_policy |
| 23 | `Variant Fulfillment Service` | Archived source only (no fulfillment integration) |
| 24 | `Variant Price` | source_price_text; KWD only if explicitly confirmed |
| 25 | `Variant Compare At Price` | product_variants.compare_at_price_text |
| 26 | `Variant Requires Shipping` | product_variants.requires_shipping |
| 27 | `Variant Taxable` | product_variants.taxable |
| 28 | `Variant Barcode` | product_variants.barcode |
| 29 | `Image Src` | media_assets.original_url + product_images |
| 30 | `Image Position` | product_images.source_position |
| 31 | `Image Alt Text` | product_images.source_alt_text (not auto-corrected) |
| 32 | `Gift Card` | Archived source only (store gift card not implemented) |
| 33 | `SEO Title` | products.seo_title |
| 34 | `SEO Description` | products.seo_description |
| 35 | `Google Shopping / Google Product Category` | Archived source only (marketing feed out of scope) |
| 36 | `Google Shopping / Gender` | Archived source only (marketing feed out of scope) |
| 37 | `Google Shopping / Age Group` | Archived source only (marketing feed out of scope) |
| 38 | `Google Shopping / MPN` | Archived source only (marketing feed out of scope) |
| 39 | `Google Shopping / Condition` | Archived source only (marketing feed out of scope) |
| 40 | `Google Shopping / Custom Product` | Archived source only (marketing feed out of scope) |
| 41 | `Google Shopping / Custom Label 0` | Archived source only (marketing feed out of scope) |
| 42 | `Google Shopping / Custom Label 1` | Archived source only (marketing feed out of scope) |
| 43 | `Google Shopping / Custom Label 2` | Archived source only (marketing feed out of scope) |
| 44 | `Google Shopping / Custom Label 3` | Archived source only (marketing feed out of scope) |
| 45 | `Google Shopping / Custom Label 4` | Archived source only (marketing feed out of scope) |
| 46 | `Bought together products (product.metafields.globo--filter--product_recommendation.bought_together_products)` | Archived source only (unverified metafield) |
| 47 | `Complementary products (product.metafields.globo--filter--product_recommendation.complementary_products)` | Archived source only (unverified metafield) |
| 48 | `Related products (product.metafields.globo--filter--product_recommendation.related_products)` | Archived source only (unverified metafield) |
| 49 | `Product rating count (product.metafields.reviews.rating_count)` | Archived source only (unverified metafield) |
| 50 | `Color (product.metafields.shopify.color-pattern)` | Archived source only (unverified metafield) |
| 51 | `Item condition (product.metafields.shopify.item-condition)` | Archived source only (unverified metafield) |
| 52 | `Item material (product.metafields.shopify.item-material)` | Archived source only (unverified metafield) |
| 53 | `Manufacturer type (product.metafields.shopify.manufacturer-type)` | Archived source only (unverified metafield) |
| 54 | `Software features (product.metafields.shopify.software-features)` | Archived source only (unverified metafield) |
| 55 | `Vehicle oil features (product.metafields.shopify.vehicle-oil-features)` | Archived source only (unverified metafield) |
| 56 | `Vehicle type (product.metafields.shopify.vehicle-type)` | Archived source only (unverified metafield) |
| 57 | `Complementary products (product.metafields.shopify--discovery--product_recommendation.complementary_products)` | Archived source only (unverified metafield) |
| 58 | `Related products (product.metafields.shopify--discovery--product_recommendation.related_products)` | Archived source only (unverified metafield) |
| 59 | `Related products settings (product.metafields.shopify--discovery--product_recommendation.related_products_display)` | Archived source only (unverified metafield) |
| 60 | `Search product boosts (product.metafields.shopify--discovery--product_search_boost.queries)` | Archived source only (unverified metafield) |
| 61 | `Variant Image` | media_assets.original_url + variant_images |
| 62 | `Variant Weight Unit` | product_variants.weight_unit |
| 63 | `Variant Tax Code` | Preserved only in archived original CSV; not interpreted by Academic V1 importer |
| 64 | `Cost per item` | Archived source only (confidential merchant cost not used) |
| 65 | `Included / Kuwait` | variant_market_availability (KW) |
| 66 | `Price / Kuwait` | variant_market_availability.raw_price (KW) |
| 67 | `Compare At Price / Kuwait` | variant_market_availability.raw_compare_price (KW) |
| 68 | `Included / International` | variant_market_availability (INT) |
| 69 | `Price / International` | variant_market_availability.raw_price (INT) |
| 70 | `Compare At Price / International` | variant_market_availability.raw_compare_price (INT) |
| 71 | `Included / Saudi Arabia` | variant_market_availability (SA) |
| 72 | `Price / Saudi Arabia` | variant_market_availability.raw_price (SA) |
| 73 | `Compare At Price / Saudi Arabia` | variant_market_availability.raw_compare_price (SA) |
| 74 | `Status` | products.status |

**Constraints:** 74 fields in the supplied structural sample; four rows only. The importer validates a subset of required core headers and tolerates additional Shopify columns for forward compatibility. Unrecognized fields remain in the source archive. DO NOT infer compatibility from Body HTML, infer prices as KWD, treat demo quantity as live stock, or treat repeated incorrect image alt text as correct.