# Phase 11 — Installation, verification and hand-in

MHAM AUTO | Academic V1 | Phase 11 | Release candidate RC1 | 28 October 2026


## 1. Prerequisites
- Windows PowerShell 5.1+, Java 21, Maven 3.9.11, Tomcat 10.1.61, MySQL 8.0.x, MySQL Workbench, VS Code
- MySQL database `mham_auto` and a least-privilege local user; never put DB passwords inside ZIP/WAR
- Export an SQL backup and copy the project folder before any source deployment or migration
- Use the *existing* local setup if database and migrations V001–V006 are already installed. Do not reset them.

## 2. Apply Phase 11 to an existing working Phase 10 project
Download `MHAM_AUTO_Phase11_Release_Overlay.zip` into Downloads, then:
```powershell
$project = 'D:\Java Project\MHAM AUTO'
$zip = Join-Path $env:USERPROFILE 'Downloads\MHAM_AUTO_Phase11_Release_Overlay.zip'
if (!(Test-Path -LiteralPath $project)) { throw "Project folder missing" }
if (!(Test-Path -LiteralPath $zip)) { throw "ZIP missing from Downloads" }
$backup = 'D:\Java Project\MHAM_AUTO_PrePhase11_' + (Get-Date -Format 'yyyyMMdd_HHmmss')
Copy-Item -LiteralPath $project -Destination $backup -Recurse -Force
Expand-Archive -LiteralPath $zip -DestinationPath $project -Force
Set-Location -LiteralPath $project
```
NOTE: Preserve your local DB backups separately; the copy above covers source files only. If you made manual customizations since Phase 10, compare/merge before overwriting.

## 3. Fresh installation / full source reconstruction
Only for a NEW directory, extract `MHAM_AUTO_AcademicV1_Full_Source_RC1.zip` into `D:\Java Project\MHAM AUTO`. This contains **source**, not `target/` or a compiled WAR.

Create the schema by executing `database/00_create_database.sql` in MySQL Workbench. Use a local MySQL account and grant only needed local privileges. Set credentials in the current PowerShell session:
```powershell
$env:MHAM_DB_USER = 'mham_app'
$secure = Read-Host 'MySQL password' -AsSecureString
$ptr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
try { $env:MHAM_DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr) }
finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr) }
Remove-Variable secure, ptr
```
Never use these plain environment variables on a publicly reachable production host. The default DB JDBC URL uses local, non-TLS MySQL and is for developer use only.

For either fresh or existing installation, build then **run migrations in exact version order**, making sure the MySQL schema exists:
```powershell
Set-Location -LiteralPath 'D:\Java Project\MHAM AUTO'
mvn clean test package dependency:copy-dependencies '-DincludeScope=runtime' '-DoutputDirectory=target/dependency'
if ($LASTEXITCODE -ne 0) { throw 'Maven/JUnit build failed' }
$cp = 'target/classes;target/dependency/*'
$migrations = @(
  'com.mhamauto.cli.CatalogCli',   # V001
  'com.mhamauto.cli.IdentityCli',  # V002
  'com.mhamauto.cli.CartCli',      # V003
  'com.mhamauto.cli.CheckoutCli',  # V004
  'com.mhamauto.cli.AdminCli',     # V005
  'com.mhamauto.cli.TradeCli'      # V006
)
foreach ($cli in $migrations) {
    & java -cp $cp $cli migrate
    if ($LASTEXITCODE -ne 0) { throw "Migration failed: $cli" }
}
```
For an existing tested database, the commands should be idempotent/checksum-aware; investigate checksum conflicts rather than modifying older migration files. MySQL DDL is not fully transactionally reversible; take a backup first.

Fresh test data import **only when the database is initially empty** (do not reimport when your source data is established):
```powershell
java -cp $cp com.mhamauto.cli.CatalogCli dry-run '--file=samples/product data sample.csv'
if ($LASTEXITCODE -ne 0) { throw 'CSV validation failed' }
java -cp $cp com.mhamauto.cli.CatalogCli import '--file=samples/product data sample.csv'
if ($LASTEXITCODE -ne 0) { throw 'Four-row sample import failed' }
# Optional: synthetic dataset test (not real products)
java -cp $cp com.mhamauto.cli.CatalogCli import '--file=samples/SYNTHETIC_TEST_500.csv'
if ($LASTEXITCODE -ne 0) { throw 'Synthetic dataset import failed' }
```
The source CSV has 4 real sample rows; the synthetic dataset has 500 rows. External Shopify image links are referenced, never stored as binary objects in MySQL. Original CSV prices are NOT assumed to be KWD. For demo checkout, consult `docs/PHASE_6_GUIDE.md` for optional, clearly fictional KWD test-price assignment.

## 4. Run release gates
```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File '.\scripts\Test-Phase11-Offline.ps1'
if ($LASTEXITCODE -ne 0) { throw 'Offline gate FAILED' }
mvn clean test package dependency:copy-dependencies '-DincludeScope=runtime' '-DoutputDirectory=target/dependency'
if ($LASTEXITCODE -ne 0) { throw 'Maven gate FAILED' }
powershell -NoProfile -ExecutionPolicy Bypass -File '.\scripts\Test-Phase11-Database.ps1'
if ($LASTEXITCODE -ne 0) { throw 'Database gate FAILED' }
```
A test run that was skipped, not executable or not evidenced is **NOT PASS**.

## 5. Deploy WAR and run HTTP verification
```powershell
$tomcat = 'D:\Java Tools\apache-tomcat-10.1.61'
& "$tomcat\bin\shutdown.bat"
# Wait and confirm the old Tomcat process has stopped before replacing the WAR.
Copy-Item -LiteralPath '.\target\mham-auto.war' -Destination "$tomcat\webapps\mham-auto.war" -Force
& "$tomcat\bin\startup.bat"
powershell -NoProfile -ExecutionPolicy Bypass -File '.\scripts\Test-Phase11-Live.ps1'
if ($LASTEXITCODE -ne 0) { throw 'HTTP gate FAILED' }
```
Tomcat must inherit `MHAM_DB_USER` / `MHAM_DB_PASSWORD` from the same session used to launch it, or from your configured local service environment. If an old exploded webapp masks the new WAR, stop Tomcat and follow your Tomcat redeploy procedure; never delete the database as part of redeployment.

## 6. Authenticated, manual UAT gate
Execute the detailed cases in `docs/PHASE_11_TEST_MATRIX.csv` with a separate customer A, customer B and administrator. Check cross-account denial; add-to-cart; checkout with fictional verified-demo KWD prices; immutable order snapshots; admin CRUD and review; business application lifecycle and access boundaries. Capture screenshots in `evidence/screenshots/` and test logs under `evidence/results/`. Do NOT include user email addresses, actual passwords, authentic addresses or personal customer data in hand-in screenshots.

## 7. Academic hand-in and exit criteria
Edit student/institution/guide details in the report, fill screenshots and replace `NOT RUN` in the matrix with observed outcomes; supply the full source ZIP, compiled WAR built from tested source, SQL scripts, report DOCX/PDF, screenshots and executed test report. Keep the project labelled Release Candidate until every mandatory test passes and an actual decision is recorded.

## Troubleshooting
- `mvn` missing: install/configure Maven 3.9.11 and Java 21; check `mvn -version`.
- DB 503: check `MHAM_DB_*`, local MySQL running, and database migration status. Never print credentials.
- JSP errors: inspect Tomcat `logs` and Maven build; check JSTL dependencies in `pom.xml`.
- 403 on form: ensure the POST came from the same origin and includes the page's CSRF token.
- Empty catalogue: run read-only DB status and confirm products are active, published and Kuwait-visible.
- Checkout refuses a price: source currency unverified. Use sanctioned synthetic-demo KWD assignments only for academic tests.
