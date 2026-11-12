# MHAM AUTO Academic V1 — Phase 6 installation & verification

## Purpose / scope

Incremental overlay on verified Phase 5. Kuwait addresses, DEMO checkout, order snapshots and user-scoped order confirmation. **No payments, tax collection, shipping, real reservations or inventory deduction.** Full order-history UI is Phase 7. No other phases redesigned.

## Inventory of Phase 6 UI

- `/addresses` GET/POST: create, edit, delete up to ten saved Kuwait addresses, user-scoped and CSRF-protected.
- `/checkout` GET/POST: authenticated order review, server-side exact KWD pricing, transactional order creation, duplicate submission protection.
- `/order-confirmation?ref=DEMO-...`: owner-scoped immutable order confirmation.
- `/cart` and `/account` gain links to the new workflow; original pages and importer remain intact.

## Before applying

1. Confirm your **Phase 5** checkout/cart foundation is tested on localhost and you can build `mham-auto.war`.
2. **Back up both the project and MySQL `mham_auto` database.** V004 is append-only and does not drop/modify old tables, but MySQL DDL commits automatically. Export via MySQL Workbench `Server → Data Export → mham_auto → Export to Self-Contained File` and store the backup outside the project. Inspect backup file and verify its size.
3. Download `MHAM_AUTO_Phase6_Checkout_Overlay.zip`. Do **not** extract to a new subfolder. Extraction must overlay the existing project root.
4. Only use local test accounts and dummy addresses; do not enter genuine personal contact data into an academic demonstration.

## 1. Backup and extract (Windows PowerShell)

```powershell
$project = 'D:\Java Project\MHAM AUTO'
$zip = "$env:USERPROFILE\Downloads\MHAM_AUTO_Phase6_Checkout_Overlay.zip"
$backup = "D:\Java Project\MHAM_AUTO_PrePhase6_$(Get-Date -Format yyyyMMdd_HHmmss)"
Copy-Item -LiteralPath $project -Destination $backup -Recurse -Force
Expand-Archive -LiteralPath $zip -DestinationPath $project -Force
Set-Location -LiteralPath $project
```

## 2. Build and apply the one-time V004 migration

In the PowerShell session where `MHAM_DB_USER` and `MHAM_DB_PASSWORD` are already set:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File '.\scripts\Test-Phase6-Offline.ps1'
if ($LASTEXITCODE -ne 0) { throw 'Phase 6 offline checks failed' }
mvn clean package dependency:copy-dependencies '-DincludeScope=runtime' '-DoutputDirectory=target/dependency'
if ($LASTEXITCODE -ne 0) { throw 'Maven build failed' }
$cp = 'target/classes;target/dependency/*'
java -cp $cp com.mhamauto.cli.CheckoutCli migrate
if ($LASTEXITCODE -ne 0) { throw 'V004 migration failed: inspect output before retrying' }
java -cp $cp com.mhamauto.cli.CheckoutCli status
if ($LASTEXITCODE -ne 0) { throw 'V004 verification failed' }
```

Migration: `database/07_checkout_schema.sql`. The script will refuse to run if V003 was not applied. Applying it a second time with the **same checksum** is safe and reports it already applied. Do not edit applied migration scripts. `database/08_phase6_validation.sql` contains optional read-only MySQL Workbench checks.

### Demo prices: why checkout may be blocked

Shopify CSV `Variant Price` was **not verified as KWD**, and Phase 2 correctly leaves `retail_price_kwd` null. Phase 6 **will not** infer conversion or charge a zero/unknown amount. For academic checkout demonstration, you can explicitly assign a fictional KWD 10.000 to **synthetic test products only** (ones with imported `[SYNTHETIC TEST ...]` titles). This will not change any of the four real source-sample product prices and will not change the raw CSV source fields.

**Optional and explicit; modifies synthetic-test rows in your database:**

```powershell
$cp = 'target/classes;target/dependency/*'
java -cp $cp com.mhamauto.cli.DemoPriceCli seed-synthetic --confirm-fictional-kwd
if ($LASTEXITCODE -ne 0) { throw 'Synthetic demo price assignment failed' }
java -cp $cp com.mhamauto.cli.CheckoutCli sample-priced
```

If your source currency is confirmed KWD, use the previously approved price-verification workflow instead. Do not run the fictional-price command on real inventory. The demo-price assignments are auditable in `demo_price_assignments`. Only synthetic product rows are eligible for this command.

## 3. Deploy the WAR (same configured Tomcat)

```powershell
$tomcat = 'D:\Java Tools\apache-tomcat-10.1.61'
& "$tomcat\bin\shutdown.bat"
Start-Sleep -Seconds 6
Copy-Item -LiteralPath '.\target\mham-auto.war' -Destination "$tomcat\webapps\mham-auto.war" -Force
& "$tomcat\bin\startup.bat"
```

The Tomcat process must receive the database environment variables, just like Phases 2–5. If Tomcat runs as a Windows service rather than being started by this PowerShell session, configure the credentials for that service separately. Confirm that the old process is stopped before replacing the WAR.

## 4. Visit & verify

- `http://localhost:8080/mham-auto/` homepage unchanged
- `http://localhost:8080/mham-auto/catalog` Phase 1.3 preview unchanged
- `http://localhost:8080/mham-auto/products` Phase 3 catalogue unchanged
- `http://localhost:8080/mham-auto/register` / `/login` Phase 4 authentication unchanged
- `http://localhost:8080/mham-auto/cart` Phase 5 persistent cart, now with conditional demo checkout link
- `http://localhost:8080/mham-auto/addresses` Kuwait address book
- `http://localhost:8080/mham-auto/checkout` review demo checkout
- `http://localhost:8080/mham-auto/order-confirmation?ref=DEMO-...` owner-scoped confirmation, after a demo order is created

For automated tests (creates two **test accounts**, one dummy saved address and **one** demo order in your local database; uses an already KWD-priced catalogue variant):

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File '.\scripts\Test-Phase6.ps1' -SkipBuild
if ($LASTEXITCODE -ne 0) { throw 'Phase 6 live verification failed' }
```

To skip HTTP and only check local Java rules plus DB migration status/selection:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File '.\scripts\Test-Phase6.ps1' -SkipBuild -SkipHttp
```

## Phase 6 acceptance criteria

1. Owner can add/edit/delete a Kuwait address; invalid governorates, phone numbers and CSRF are rejected.
2. User cannot read or change another user's address through guessed IDs.
3. Checkout rejects unpriced, unavailable or empty cart and never accepts a client-entered price or total.
4. Checkout creates a **single** durable order with KWD unit-price, SKU, title, quantity and address snapshots in one transaction; clears only that customer's cart.
5. Repeated POST of the same checkout token returns the **same** order reference, no duplicate order.
6. Guest is redirected to login; different customer cannot view order confirmation.
7. No payment attempts, inventory decrement or real shipment records are produced.
8. Original homepage, catalogue, registration/login, importer and cart regression checks pass.

### What is and is not verified here

Offline Java source checks and plain rules can be performed without MySQL. Full Maven/JUnit, JSP compilation, live MySQL migration and HTTP workflows **must pass on your Windows system**. Do not mark Phase 6 complete until these pass and you confirm the results.

## Known boundaries

- Full order-history list, customer profile improvements, saved address defaults: Phase 7.
- Admin order statuses: Phase 8.
- B2B company applications: Phase 9.
- Real KNET payments, VAT, shipment and verified inventory: NOT Academic V1.
- This is academic demo software, not a live Kuwait store.
