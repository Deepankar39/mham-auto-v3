# MHAM AUTO Phase 8 — Administration (Academic V1)

Apply only after Phases 1.3–7 have been installed, including the V004 Checkout migration. The Phase 8 ZIP is **an overlay, not a replacement project**. Back up both the entire project and the MySQL `mham_auto` schema first.

## Capabilities
- `/admin`: dashboard with counts of products, variants, sample stock, categories and demo orders.
- `/admin/products`: 20-item paginated list filtered by SKU/handle prefixes.
- `/admin/product`: create/edit first variant of a product; archive products rather than deleting referenced records.
- `/admin/categories`: add categories.
- `/admin/orders`: paginated sample orders with status filtering.
- `/admin/order?id=...`: immutable order snapshots and placed→reviewed/cancelled demo status transitions.

## Safety boundaries
- Every `/admin` route is protected by the existing `ProtectedPagesFilter`; it refreshes active account status and ADMIN role from the database on **each** request. AdminServlet checks role again.
- All POST forms use existing CSRF synchronizer tokens; all writes use prepared statements and transactions.
- Change records are stored in V005 `admin_events` (`actor_user_id`, action, target ID, change summary). No payment processing or fulfilment is performed.
- Input stock remains **sample / unverified**, not verified live stock; sample checkout prices require an explicit fictional KWD confirmation and provenance in `demo_price_assignments`.
- Imported `handle` never changes on edit. Existing source HTML is retained if the description field is left blank on edit.
- **Only first variant** is edited from the admin form, to avoid silently rewriting Shopify multi-variant product relationships. Use source CSV importer for larger variant updates.
- New optional product-image URL must pass the existing allowlisted HTTPS Shopify CDN policy; image bytes remain external.
- Order snapshots are never recomputed or modified.

## Installation — PowerShell

1. Download `MHAM_AUTO_Phase8_Admin_Management_Overlay.zip` to Downloads.
2. Backup local files and MySQL. In PowerShell:

```powershell
$project = 'D:\Java Project\MHAM AUTO'
$zip = "$env:USERPROFILE\Downloads\MHAM_AUTO_Phase8_Admin_Management_Overlay.zip"
$backup = 'D:\Java Project\MHAM_AUTO_PrePhase8_' + (Get-Date -Format yyyyMMdd_HHmmss)
Copy-Item -LiteralPath $project -Destination $backup -Recurse -Force
Expand-Archive -LiteralPath $zip -DestinationPath $project -Force
Set-Location -LiteralPath $project
```

3. Use the existing terminal session with `MHAM_DB_USER`, `MHAM_DB_PASSWORD` configured (as in Phases 2–7). Run:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File '.\scripts\Test-Phase8-Offline.ps1'
if ($LASTEXITCODE -ne 0) { throw 'Offline tests failed' }
mvn clean package dependency:copy-dependencies '-DincludeScope=runtime' '-DoutputDirectory=target/dependency'
if ($LASTEXITCODE -ne 0) { throw 'Maven test/build failed' }
$cp = 'target/classes;target/dependency/*'
java -cp $cp com.mhamauto.cli.AdminCli migrate
if ($LASTEXITCODE -ne 0) { throw 'V005 migration failed' }
java -cp $cp com.mhamauto.cli.AdminCli status
if ($LASTEXITCODE -ne 0) { throw 'V005 verification failed' }
```

4. Stop the old Tomcat process **and verify it stopped**. Copy `target\mham-auto.war` to `D:\Java Tools\apache-tomcat-10.1.61\webapps\mham-auto.war`, then start Tomcat. The account running Tomcat must have the Phase 2 database environment variables.
5. Run the smoke script after deployment:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File '.\scripts\Test-Phase8.ps1' -SkipBuild
if ($LASTEXITCODE -ne 0) { throw 'HTTP regression failed' }
```

6. Register an account if you don't have an ADMIN user. Grant admin via the existing explicit local CLI method, NOT the public registration form:

```powershell
java -cp $cp com.mhamauto.cli.IdentityCli grant-admin 'your-email@example.com' --confirm-local-admin-bootstrap
```

Sign out and sign back in. Load `http://localhost:8080/mham-auto/admin`.

## Manual acceptance checklist
- Customer receives HTTP 403 at `/admin`, `/admin/products`, `/admin/orders`.
- Admin can open dashboard and lists.
- Create category, then create a sample product with unique handle/SKU. Confirm active + published product appears in `/products`.
- Set sample stock = 2 and fictional KWD price = 10.000 with confirmation checkbox. Add the product to customer cart and complete existing demo checkout.
- Change product name/stock or archive product; the **already-created** demo order retains its original title, SKU and price snapshots.
- Admin can review demo order and move `DEMO_PLACED` to `DEMO_REVIEWED` once. `DEMO_REVIEWED`→`DEMO_PLACED` must be rejected.
- CSRF: POST without token is HTTP 403. Forged `CUSTOMER` POST to `/admin/product` is HTTP 403.
- Creating a duplicate handle/SKU fails without partial rows; absent category ID fails.
- Re-running `AdminCli migrate` reports already applied with matching checksum.
- Existing homepage `/`, `/catalog`, `/products`, `/cart`, `/checkout`, `/orders` and `/account` remain functional.

## Development verification
New plain-Java validation and Java-source stub checks can be run offline. Full Maven dependencies, JSP compilation, MySQL transactions and Tomcat integration **must be tested in the user's local Windows setup**. Do not mark Phase 8 complete before local acceptance.
