# Evidence and screenshot capture checklist

MHAM AUTO | Academic V1 | Phase 11 | Release candidate RC1 | 28 October 2026


**Do not submit invented screenshots.** Capture each screenshot from your *locally running tested version* after confirming the expected behavior. Use invented test names, fictional Kuwait addresses and fictitious product pricing only. Blur or remove emails, real phones, DB secrets and personal information.

| ID | Evidence filename (put under `evidence/screenshots/`) | Acceptance point |
|---|---|---|
| EV01 | `01_homepage_desktop.png` | Responsive homepage at local /mham-auto/ |
| EV02 | `02_homepage_mobile.png` | Mobile navigation / focus state |
| EV03 | `03_product_catalogue.png` | MySQL products; search, category filters and paging |
| EV04 | `04_product_details.png` | Variant, SKU and external image fallback |
| EV05 | `05_registration_login.png` | Login/register page (no real secrets shown) |
| EV06 | `06_customer_account.png` | Customer dashboard and profile |
| EV07 | `07_cart.png` | Persisted cart and server-side KWD demo subtotal |
| EV08 | `08_kuwait_address.png` | Fictional Kuwait address example |
| EV09 | `09_demo_checkout.png` | Clearly labelled NO PAYMENT demo review |
| EV10 | `10_order_confirmation.png` | Demo order reference and simulated status |
| EV11 | `11_order_history.png` | Customer-owned order list and snapshots |
| EV12 | `12_admin_dashboard.png` | Authenticated ADMIN view |
| EV13 | `13_admin_product.png` | Product/category/sample inventory management |
| EV14 | `14_admin_order_review.png` | Approved demo order review state |
| EV15 | `15_trade_application.png` | Fictional business application form |
| EV16 | `16_trade_admin_review.png` | Admin approve/reject business application |
| EV17 | `17_mysql_schema.png` | MySQL Workbench schema (no private rows) |
| EV18 | `18_maven_tests.png` | Maven/JUnit success and version |
| EV19 | `19_live_http_tests.png` | Phase 11 HTTP smoke results |
| EV20 | `20_security_access_denied.png` | Admin denies non-ADMIN user |

## Log/evidence files expected under `evidence/results/`
- `phase11-offline.txt` — historical Java rules + source-layout tests
- `phase11-maven.txt` — Maven 21/JUnit/WAR compile (created by local acceptance runner)
- `phase11-database.txt` — six DB CLI read-only status checks
- `phase11-http.txt` — public and anonymous protected route checks
- `phase11-schema.txt` — Workbench validation output (manual export)
- `phase11-manual.csv` — completed test matrix with actual results

## Evidence quality requirements
- Filename matches matrix or has unambiguous ID.
- Timestamp/version environment recorded once in the report.
- Screenshots cropped to show the feature and URL without exposed passwords.
- All failing scenarios are documented, including reproduction steps and fixes.
- Manual reviewer confirms the report reflects the final tested build, not earlier phase screenshots.
