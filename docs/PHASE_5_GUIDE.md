# MHAM AUTO Academic V1 / Phase 5 — Persistent Shopping Cart

## Compatibility and scope
- Apply only over a working Phase 4 project (Phases 1.3–4 are prerequisites).
- Preserve existing Maven application and source. This is an incremental overlay, not a new project.
- No orders, checkout, shipping, payment, trade pricing or real inventory reservation is implemented. Checkout and Kuwait addresses remain Phase 6.
- Requires V001, V002 existing MySQL schema. Apply **V003** with the CLI from the project root (not by dropping/recreating the database).
- Your original Shopify CSV has prices of unknown source currency; it is safe for customers to **save** those parts in a cart, but totals read **Pending verification** unless every item has a validated `retail_price_kwd`. Stock is source/sample, not live verified.

## 1. Back up and apply the overlay in PowerShell
```powershell
$project='D:\Java Project\MHAM AUTO'
$zip="$env:USERPROFILE\Downloads\MHAM_AUTO_Phase5_Cart_Overlay.zip"
$backup='D:\Java Project\MHAM_AUTO_PrePhase5_'+(Get-Date -Format 'yyyyMMdd_HHmmss')
Copy-Item -LiteralPath $project -Destination $backup -Recurse -Force
Expand-Archive -LiteralPath $zip -DestinationPath $project -Force
Set-Location -LiteralPath $project
```
Overlay only the exact paths in `docs/PHASE_5_MANIFEST.md`. No existing catalogue, customer or source import data is deleted.

## 2. Build (with your Phase 2/4 environment variables set)
```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File '.\scripts\Test-Phase5-Offline.ps1'
if($LASTEXITCODE -ne 0){throw 'Offline test failed'}
mvn clean package dependency:copy-dependencies '-DincludeScope=runtime' '-DoutputDirectory=target/dependency'
if($LASTEXITCODE -ne 0){throw 'Maven/JUnit build failed'}
```
Ensure `MHAM_DB_USER` and `MHAM_DB_PASSWORD` exist in the current PowerShell environment; run the CLI from the **project root** so relative migration files resolve.

## 3. Apply V003 database migration once; repeat invocation verifies its checksum
```powershell
$cp='target/classes;target/dependency/*'
java -cp $cp com.mhamauto.cli.CartCli migrate
if($LASTEXITCODE -ne 0){throw 'V003 migration failed'}
java -cp $cp com.mhamauto.cli.CartCli status
if($LASTEXITCODE -ne 0){throw 'Cart schema missing'}
```
V003 uses `IF NOT EXISTS` and a migration checksum. MySQL DDL auto-commits, so back up the database first when applying to valuable data. If a migration partially fails, do not drop tables blindly; inspect and retry only after correcting the cause.

## 4. Deploy (stop Tomcat before replacing WAR)
```powershell
$tomcat='D:\Java Tools\apache-tomcat-10.1.61'
& "$tomcat\bin\shutdown.bat"
Start-Sleep -Seconds 6
Copy-Item -LiteralPath '.\target\mham-auto.war' -Destination "$tomcat\webapps\mham-auto.war" -Force
& "$tomcat\bin\startup.bat"
```
The Tomcat process must receive the same DB environment variables as the CLI. If it runs as a Windows service, set service-level environment variables rather than relying on PowerShell. Wait until WAR deployment has finished, then verify routes in the browser.

## 5. Manually verify
1. Login as customer. Visit `/products`, open a product detail, choose a variant, and add 1 unit.
2. Open `/cart`; verify line quantity, source stock note, image fallback and price status.
3. Update quantity; add a second time (adds to existing quantity). Try quantity 21 (reject).
4. Remove item; add item and clear cart; verify empty state.
5. Sign out, then sign in again: cart persists because it is stored in MySQL.
6. Login with another account; confirm no first-account cart lines are visible.
7. Run the existing homepage, `/catalog`, `/products`, `/product?handle=...`, `/account`, `/admin` and `/health` regression paths.
8. Confirm `/api/cart/count` returns a count for the signed-in user and `{\"count\":0}` for a guest.

## 6. Automated verification (creates two `.test` customer accounts)
```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File '.\scripts\Test-Phase5.ps1' -SkipBuild
if($LASTEXITCODE -ne 0){throw 'Phase 5 verification failed'}
```
The test selects an existing Kuwait-visible variant using **read-only** SQL, creates two isolated smoke test customers, tests GET/POST/cart count, logout/login persistence, CSRF, invalid quantities, cross-account separation and remove. It does not touch other users' carts. `-SkipHttp` performs only offline and DB checks.

## Troubleshooting
- `V002 missing`: run Phase 4 `IdentityCli migrate` before V003.
- MySQL access rejected: check `MHAM_DB_USER`, `MHAM_DB_PASSWORD` and local database grants.
- HTTP 503: check Tomcat DB environment and migration status; see Tomcat logs.
- HTTP 403 on POST: reload product/cart page and submit a fresh CSRF token after login.
- HTTP 409: product became inactive, quantity exceeded the 20-unit cap or the source quantity, or the cart item changed; reload the page.
- Price pending: do **not** set arbitrary KWD prices. Validate the source currency before importing confirmed KWD values in a later phase.
- The existing Phase 4 smoke test must pass too; Phase 5 cannot compensate for unverified identity setup.

## Definition of done
A successful Maven/JUnit build, V003 migration, add/update/remove/clear, CSRF rejection, quantity guardrails, per-account data isolation, logout/login persistence and homepage/catalogue regression tests, followed by user confirmation. No claim of real payment or stock reservation.
