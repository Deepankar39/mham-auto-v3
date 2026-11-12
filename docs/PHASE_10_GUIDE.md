# MHAM AUTO — Academic V1 Phase 10 Installation / Verification

## Scope
This ZIP overlays the existing Phase 9 project. It does NOT replace or reinitialize it, and it has **no database migration**. No live payments or fulfillment. The `pom.xml` and previous Phase 1–9 business services are preserved except the bounded catalogue query timeout.

### Improvements
- Global user interface refinements: keyboard focus, reduced-motion, mobile homepage navigation, overflow-safe admin tables, usable mobile form controls, print styles.
- Navigation: current page is marked accurately; mobile catalogue menu closes on outside tap, resize and Escape.
- Security: HTTP response headers include CSP, Permissions-Policy, nosniff and frame-deny; unsafe browser requests with a mismatched `Origin` are denied. Existing synchronizer-CSRF checks **remain required**; missing Origin is not treated as proof of trust.
- Strict CSP removes one legacy inline style and allows only app-hosted scripts/styles and product images from `https://cdn.shopify.com`.
- Static assets cached for five minutes with revalidation; catalogue prepared reads have a 10-second safety timeout.
- Fixes the admin product list empty-state condition and improves accessible cart-count announcements.
- Read-only SQL `EXPLAIN` review and optional local HTTP timings; neither proves performance at one million products.

## Prerequisites
- Fully installed Phase 1.3 through Phase 9 source and all corresponding MySQL migrations V001–V006.
- Windows, PowerShell, Java 21, Maven 3.9.11, MySQL and Tomcat 10.1.61.
- `MHAM_DB_USER` and `MHAM_DB_PASSWORD` available to the **Tomcat process**.
- `http://localhost:8080/mham-auto/` working before applying the overlay.

## 1. Backup + extract
Export your `mham_auto` MySQL database using Workbench. Backup the project and install:

```powershell
$project = 'D:\Java Project\MHAM AUTO'
$zip = "$env:USERPROFILE\Downloads\MHAM_AUTO_Phase10_UX_Security_Performance_Overlay.zip"
$backup = 'D:\Java Project\MHAM_AUTO_PrePhase10_' + (Get-Date -Format yyyyMMdd_HHmmss)
Copy-Item -LiteralPath $project -Destination $backup -Recurse -Force
Expand-Archive -LiteralPath $zip -DestinationPath $project -Force
Set-Location -LiteralPath $project
```

## 2. Validate/build
```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File '.\scripts\Test-Phase10-Offline.ps1'
if ($LASTEXITCODE -ne 0) { throw 'Phase 10 offline test failed' }
mvn clean test package
if ($LASTEXITCODE -ne 0) { throw 'Maven build failed' }
```

## 3. Deploy
Stop Tomcat completely; copy `target\mham-auto.war` to `D:\Java Tools\apache-tomcat-10.1.61\webapps\mham-auto.war` and restart Tomcat. Ensure you use the same database environment as earlier phases. If Tomcat holds the old exploded app, **only after backing up** remove `webapps\mham-auto` while Tomcat is stopped so Tomcat can expand the new WAR; never touch your MySQL data.

## 4. Live HTTP tests
```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File '.\scripts\Test-Phase10.ps1' -SkipBuild
if ($LASTEXITCODE -ne 0) { throw 'Phase 10 HTTP test failed' }
powershell -NoProfile -ExecutionPolicy Bypass -File '.\scripts\Measure-Phase10.ps1' -Runs 5
```
HTTP tests require the imported catalogue and DB status to be working, and test anonymous redirects. They do not replace authenticated user testing.

## 5. Manual regression / UX checklist
1. Browse homepage, preview, DB catalogue and product details on narrow mobile and desktop widths; verify no unusable overflow.
2. Keyboard Tab navigation: visible focus rings; skip links; mobile menu opens, closes by Escape and outside tap.
3. Register/login/logout; manage profile/address; cart add/update/remove; demo checkout; own order history.
4. As ADMIN: product/category CRUD, inventory, demo order statuses, trade application review. As CUSTOMER, admin routes remain inaccessible.
5. Attempt POST to login with forged Origin; expect HTTP 403. Existing CSRF submissions work when made from the local site.
6. Inspect response CSP and Permissions-Policy. Confirm Shopify images still load; external hosts and inline scripts/styles blocked by CSP.
7. Verify `GET /assets/css/phase10.css` has cache max-age=300 and non-asset protected responses are no-store.
8. Test reduced-motion setting, zoom to 200%, form error messages and empty catalogue/admin results.
9. Run previous Phase 6–9 HTTP scripts as relevant; ensure order snapshots and previous demo data are unchanged.
10. Run `database/14_phase10_performance_review.sql` in Workbench on a suitable test dataset and examine query plans.

## Important boundaries
- **No claim of production security audit or 1M-product load testing.** CSP and Origin validation are defense-in-depth, not replacements for strong authentication, CSRF, HTTPS, monitoring or a hosted security review.
- HTTP localhost is intentional for academic demo. Production must terminate HTTPS and set `Secure` session cookies, verify proxy/origin scheme, deploy HSTS only under HTTPS, and reassess CDN/Content Security Policy.
- Static assets are short-cache because the project doesn't use hashed filenames. A browser hard refresh may be necessary immediately after extraction.
- Phase 11 remains final comprehensive regression, academic report, and release packaging.
