# MHAM AUTO — Academic V1 · Phase 2 Local Setup and Acceptance

## Scope
MySQL 8 / JDBC / HikariCP + Shopify-format CSV streaming importer, 74-column input and ordered CDN image references. The original homepage and Phase 1.3 catalogue **remain preview-only** until Phase 3, so no risky production-facing price changes are introduced.

All work is **DEMONSTRATION ONLY**, not merchant-grade inventory, checkout, or fitment information. Uploaded source has **four actual sample rows**; the 500-row generator creates **synthetic test data** that must not be mistaken for real products.

## Prerequisites
- The *already tested* Phase 1.4 project under `D:\Java Project\MHAM AUTO`.
- JDK 21, Maven 3.9.11, Tomcat 10.1.61, MySQL Server 8+ and MySQL Workbench.
- Internet for the FIRST Maven build to fetch 4 new runtime libraries, SLF4J and JUnit test dependencies.
- Explicit `MHAM_DB_USER`, `MHAM_DB_PASSWORD` environment variables and (optional) `MHAM_DB_URL` in the terminal that runs the importer/Tomcat.

## Installation
1. Back up your working project directory. Extract Phase 2 overlay ZIP into the project root, preserving existing files. The ZIP includes *only new and changed files* and must NOT replace the whole app.
2. In MySQL Workbench as administrator, run `database/00_create_database.sql` (creates `mham_auto`). Create a **dedicated local DB account** with DDL/DML permissions limited to `mham_auto.*`. Example SQL to adapt and run in Workbench:

   ```sql
   CREATE USER IF NOT EXISTS 'mham_app'@'127.0.0.1' IDENTIFIED BY 'REPLACE_WITH_A_PRIVATE_STRONG_PASSWORD';
   GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, REFERENCES
     ON mham_auto.* TO 'mham_app'@'127.0.0.1';
   ```
   Replace the placeholder before execution. Do not keep your real password in project files. A MySQL account for host `localhost` might also be necessary depending on authentication/routing settings. Do not use `root` for the app.
3. In Windows PowerShell, set credentials **in that terminal session only**:

   ```powershell
   Set-Location -LiteralPath 'D:\Java Project\MHAM AUTO'
   $env:MHAM_DB_USER = 'mham_app'
   $secure = Read-Host 'Enter MySQL mham_app password' -AsSecureString
   $ptr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
   try { $env:MHAM_DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr) }
   finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr) }
   Remove-Variable secure, ptr
   ```

   The default JDBC URL is `jdbc:mysql://127.0.0.1:3306/mham_auto?sslMode=DISABLED&allowPublicKeyRetrieval=true&connectionTimeZone=UTC`. **Local development only**, not for remote or production usage. To override: `$env:MHAM_DB_URL='jdbc:mysql://...'`. A Tomcat JVM inherits environment variables only when started from the same configured environment; use a Tomcat service's actual environment configuration if you run it as a Windows service.
4. Build and stage the runtime JARs (requires online Maven the first time):

   ```powershell
   powershell -NoProfile -ExecutionPolicy Bypass -File '.\scripts\Test-Phase2-Offline.ps1'
   mvn clean package dependency:copy-dependencies '-DincludeScope=runtime' '-DoutputDirectory=target/dependency'
   ```

5. Use the app's CLI with the classpath, not a MySQL CLI requirement:

   ```powershell
   $cp = 'target/classes;target/dependency/*'
   java -cp $cp com.mhamauto.cli.CatalogCli migrate
   java -cp $cp com.mhamauto.cli.CatalogCli dry-run '--file=samples/product data sample.csv'
   java -cp $cp com.mhamauto.cli.CatalogCli import '--file=samples/product data sample.csv'
   java -cp $cp com.mhamauto.cli.CatalogCli stats
   ```

   **The uploaded CSV does not declare its base currency.** Source raw prices are retained as text; `source_currency` and `retail_price_kwd` remain NULL. This is intentional. Only if the file's currency is **independently confirmed to be KWD**, pass `--source-currency=KWD`; otherwise do not use it. No automatic FX conversion exists.

6. A ready-to-import **500 synthetic-row fixture** is included at `samples/SYNTHETIC_TEST_500.csv` so you can test immediately. You may also regenerate it from the 74-column source contract using the Java generator:

   ```powershell
   java -cp $cp com.mhamauto.cli.CatalogCli generate-demo '--file=samples/product data sample.csv' '--output=samples/SYNTHETIC_TEST_500.csv' '--count=500'
   java -cp $cp com.mhamauto.cli.CatalogCli dry-run '--file=samples/SYNTHETIC_TEST_500.csv'
   java -cp $cp com.mhamauto.cli.CatalogCli import '--file=samples/SYNTHETIC_TEST_500.csv'
   java -cp $cp com.mhamauto.cli.CatalogCli stats
   ```

   Synthetic rows have unique handles and SKUs, but reuse sample Shopify CDN image URLs for testing. Do not present these as distinct verified product photographs or actual inventory. Generator does NOT fetch image bytes, alter the source, or access the database.

7. Deploy `target/mham-auto.war` using your established Tomcat process. Stop Tomcat gracefully before replacing its WAR if hot deployment is unreliable; start it again with the same database environment. Test homepage `/mham-auto/`, preview `/mham-auto/catalog`, existing `/mham-auto/health` and new `/mham-auto/api/db-status` (HTTP 200 with `DB READY`; 503 without configured MySQL). The existing homepage/catalogue must still work with MySQL down.

8. For **reimport idempotence and DB readiness**, run:

   ```powershell
   powershell -NoProfile -ExecutionPolicy Bypass -File '.\scripts\Test-Phase2.ps1'
   ```

   If you have not deployed the new WAR yet, temporarily use `-SkipHttp` and then manually test the endpoint after deployment. The script intentionally re-imports the four-row sample twice into the local demo database and checks product/variant/media counts do not grow from duplicates.

## Import design notes
- Rows are streamed with Apache Commons CSV, not loaded as a 1M-row Java list; batch transaction commits default to 250 rows.
- `Handle` identifies product; `Variant SKU` identifies a sellable variant when present. Image-only rows have no SKU and are still linked to their product.
- Repeated handles keep one product; same SKU with conflicting product owner is rejected. Original SKU strings remain intact, including leading apostrophes.
- `Image Src` links to an ordered product image; `Variant Image` attaches to that variant. The same `media_assets` URL can belong to multiple products. No image binary is downloaded or stored.
- Allowed external links are HTTPS `cdn.shopify.com`. Other links cause a logged warning and are not rendered by the importer. Incorrect alt text remains in `source_alt_text` for manual correction before production.
- Records without SKU can still contain product information/image; product metadata must occur before its image-only records, as with Shopify handle groups. Blank titles on subsequent rows do not erase an existing product.
- Every job records SHA-256 of the CSV file, counts, statuses, rejected rows and warnings. Preserve an offline copy of each original CSV in your approved project data archive; raw unmodeled source columns are **not** all duplicated into MySQL.
- Reimport is additive/idempotent for repeated product handles, SKUs, URLs and option relations. **It intentionally does not delete records absent from later imports** (avoid accidental deletion on partial exports).
- Failed rows use savepoints so the batch can continue. If the DB connection fails entirely, the import fails; rerun the same file safely after resolving the issue.
- Migration V001 is checksum tracked. SQL DDL autocommits in MySQL; failed first-time migrations may leave some tables. DDL uses `IF NOT EXISTS` for repeat application, but schema drift still requires investigation. Back up important DB data before future migrations.

## Expected results on a previously empty database after the 4-row source import
- 4 products; 4 variants; 4 product-image associations; 4 unique media URLs; 4 inventory records; 2 apostrophe-prefixed SKUs preserved.
- No verified KWD prices because source currency is UNKNOWN.
- One or more import warning logs for mismatched alt text and apostrophe-prefixed SKUs.
- `SELECT * FROM import_jobs` reports a COMPLETED job if all rows satisfy constraints.
- Rerunning identical CSV does not duplicate products/variants/images (only adds another import job and warning/error audit records).

## Phase 2 exit criteria
(1) Phase 1.4 regressions pass; (2) Maven JUnit tests and WAR build pass; (3) V001 migration is applied; (4) real sample and synthetic 500-row imports succeed; (5) repeat import counts stable; (6) new readiness endpoint works; (7) site remains usable with MySQL unavailable. **The full 50k/1M benchmark is later work and is not claimed as tested here.**
