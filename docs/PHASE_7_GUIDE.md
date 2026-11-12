# MHAM AUTO — Phase 7 | Customer Dashboard and Order History

## Installation safety
1. This ZIP is an **overlay** over the tested Phase 6 project. Do not extract into a new Maven project or remove previous modules.
2. Back up `D:\Java Project\MHAM AUTO` and export your MySQL `mham_auto` database before replacing files.
3. Extract the ZIP into that project root, preserving its `src/...`, `scripts/...`, `tests/...`, and `docs/...` relative directories.
4. **No new SQL migration**: existing Phase 4 users, Phase 5 carts, and Phase 6 `demo_orders`, `demo_order_items`, `customer_addresses` tables and indexes are reused. Do not rerun initialization scripts unnecessarily.

## Commands (PowerShell)
```powershell
Set-Location -LiteralPath 'D:\Java Project\MHAM AUTO'
powershell -NoProfile -ExecutionPolicy Bypass -File '.\scripts\Test-Phase7-Offline.ps1'
if ($LASTEXITCODE -ne 0) { throw 'Offline tests failed' }
mvn clean package dependency:copy-dependencies '-DincludeScope=runtime' '-DoutputDirectory=target/dependency'
if ($LASTEXITCODE -ne 0) { throw 'Maven/JUnit failed' }
$cp='target/classes;target/dependency/*'
java -cp $cp com.mhamauto.cli.CheckoutCli status
if ($LASTEXITCODE -ne 0) { throw 'Phase 6 schema missing' }
```

Deploy `target/mham-auto.war` to your configured Tomcat 10.1.61 installation using your established STOP → DEPLOY → START process. Tomcat requires your existing Phase 2 environment variables. Avoid copying WAR while the application is still running.

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File '.\scripts\Test-Phase7.ps1' -SkipBuild
if ($LASTEXITCODE -ne 0) { throw 'Phase 7 HTTP tests failed' }
```
The HTTP script handles expected 400/403/404 responses and is compatible with Windows PowerShell 5.1.

Optional: open `database/09_phase7_validation.sql` in MySQL Workbench. It contains **read-only** validation queries, not DDL.

## New routes
- `GET /mham-auto/account` — dashboard and latest five orders; existing `/admin` unchanged.
- `POST /mham-auto/account` — CSRF-protected display-name update (`action=updateName`), then 303 redirect.
- `GET /mham-auto/orders?page=1` — customer order history, newest first, 12 entries per page.
- `GET /mham-auto/orders/view?ref=DEMO-...` — details using original immutable Phase 6 snapshot (owner restricted).
- Orders do not expose POST update endpoints. Demo status updates are reserved for Phase 8 admin.

## Tests and manual checklist
- Existing homepage, `/catalog`, `/products`, `/cart`, `/addresses`, `/checkout`, `/order-confirmation` still work.
- Register two accounts. Each sees only their own demo orders; an unowned reference must give HTTP 404.
- Place a Phase 6 demo order with a synthetic test variant that has explicitly approved fictional KWD demo pricing. Check dashboard recent item, history and detail snapshots; verify `KWD` 3-decimal formatting.
- Edit a saved address, then view a prior demo order: historical address and price remain unchanged.
- POST to `/account` without a correct CSRF token must return 403.
- Rename account; logout/login and confirm the new display name persists. Email remains read-only.
- Guest `/orders` redirects to login. Valid session but other user's order returns 404. Invalid page/reference returns 400.
- Demo totals represent **historical simulated order totals**, never sales revenue or actual payments.

## Performance and constraints
- Existing Phase 6 composite index `(user_id,created_at,id)` supports account-scoped chronological history.
- Orders are accessed with limited page sizes and bounded offset pagination. For many-thousand-order individual accounts, implement keyset pagination in V2.
- Dashboard counts only the current account, returns latest five; no binary images or additional image downloads.
- No database migrations, destructive SQL, payment handling, fulfilment or inventory deduction in Phase 7.
- Phase 8: admin dashboard + product and demo-order management; Phase 9: B2B application and review; Phase 10: hardening; Phase 11: final academic release.
