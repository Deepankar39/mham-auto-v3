# MHAM AUTO — Phase 9 / Simple B2B Application and Admin Review

## Scope

Academic V1 **only**. Customer submits one company/contact request; admin makes one PENDING → APPROVED or REJECTED review decision; customer sees own status. NO wholesale entitlements, discounts, credit, RFQ, company users, file uploads, real verification or live commerce.

## Baseline and safety

Prerequisite: Phases 1.3 through 8 extracted and successfully built, Phase 8 V005 applied. ZIP is an *incremental overlay*. Back up project folder and MySQL database with MySQL Workbench Data Export before applying. Do not extract as a separate Maven project. The ZIP adds V006 without editing earlier SQL migration files.

## Installation — Windows PowerShell

Run in a fresh PowerShell shell as needed, then configure MHAM_DB_USER and MHAM_DB_PASSWORD as previously instructed in Phase 2 (do not commit credentials to source). Set environment variables in the PowerShell session that launches Tomcat.

```powershell
$project = 'D:\Java Project\MHAM AUTO'
$zip = "$env:USERPROFILE\Downloads\MHAM_AUTO_Phase9_B2B_Applications_Overlay.zip"
$backup = "D:\Java Project\MHAM_AUTO_PrePhase9_$(Get-Date -Format yyyyMMdd_HHmmss)"
Copy-Item -LiteralPath $project -Destination $backup -Recurse -Force
Expand-Archive -LiteralPath $zip -DestinationPath $project -Force
Set-Location -LiteralPath $project

powershell -NoProfile -ExecutionPolicy Bypass -File '.\scripts\Test-Phase9-Offline.ps1'
if ($LASTEXITCODE -ne 0) { throw 'Phase 9 offline tests failed' }
mvn clean test package dependency:copy-dependencies '-DincludeScope=runtime' '-DoutputDirectory=target/dependency'
if ($LASTEXITCODE -ne 0) { throw 'Maven build failed' }
$cp = 'target/classes;target/dependency/*'
java -cp $cp com.mhamauto.cli.TradeCli migrate
if ($LASTEXITCODE -ne 0) { throw 'V006 migration failed' }
java -cp $cp com.mhamauto.cli.TradeCli status
if ($LASTEXITCODE -ne 0) { throw 'V006 status failed' }
```

Database script: `database/12_trade_application_schema.sql`; read-only Workbench verification: `database/13_phase9_validation.sql`. The migration is re-runnable and SHA-256-checksummed. MySQL DDL auto-commits; have a backup ready.

### Deploy the WAR

```powershell
$tomcat = 'D:\Java Tools\apache-tomcat-10.1.61'
& "$tomcat\bin\shutdown.bat"
# Ensure the previous Tomcat Java process really exits. Do not delete the MySQL database.
Copy-Item '.\target\mham-auto.war' "$tomcat\webapps\mham-auto.war" -Force
& "$tomcat\bin\startup.bat"
```

**Tomcat must inherit MHAM_DB_USER / MHAM_DB_PASSWORD**. If Tomcat was installed as a Windows service, use its service environment configuration rather than assuming interactive PowerShell credentials are inherited. Allow Tomcat to redeploy, then run:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File '.\scripts\Test-Phase9.ps1' -SkipBuild
if ($LASTEXITCODE -ne 0) { throw 'Phase 9 HTTP smoke test failed' }
```

## Pages

- Customer application: `http://localhost:8080/mham-auto/trade/apply`
- Customer-owned status: `http://localhost:8080/mham-auto/trade/status`
- Admin list/filter: `http://localhost:8080/mham-auto/admin/trade-applications`
- Admin detail/review: `http://localhost:8080/mham-auto/admin/trade-application?id=ID`
- Account, cart, checkout, order history, products and earlier admin pages should continue working unchanged.

## Manual acceptance procedure (required)

1. As anonymous visitor, opening `/trade/apply` or `/trade/status` redirects to `/login`.
2. As **Customer A**, submit an application using fictitious business name and a valid-format Kuwait phone number. Verify PRG and status PENDING on `/trade/status`.
3. Revisit `/trade/apply`: it should redirect to existing status, not create a duplicate. A direct second POST must fail with HTTP 409.
4. As **Customer B**, submit a different application. Customer B must only see Customer B's status, never Customer A's.
5. As ordinary customer, direct GET/POST on `/admin/trade-applications` and `/admin/trade-application` must be forbidden (403). Anonymous must redirect to login.
6. As ADMIN, open list, filter PENDING, open Customer A's application, approve with an optional *private* note, and verify it appears APPROVED.
7. Verify Customer A sees APPROVED on `/trade/status` but **cannot see admin-only notes**, and still receives only ordinary CUSTOMER privileges (NO trade prices/credit).
8. Re-submit the same admin POST (fresh valid CSRF): it must fail with 409 rather than emit a duplicate decision event.
9. Review Customer B's application as REJECTED and verify B sees REJECTED. No user-role grants may occur.
10. Check DB in Workbench: one row per applicant, one SUBMITTED event per application, one decision event per reviewed application. Re-run `TradeCli migrate` to confirm checksum/idempotence.
11. Retest homepage, products, cart, demo checkout, My Orders and existing admin product/order management.

### Database assertions (read-only)

Use `database/13_phase9_validation.sql`. Also check that `roles` still only contains intended prior roles, `user_roles` was not modified by trade reviews, and order/product totals match your pre-migration backup.

## Design/security notes

- DB unique `user_id` ensures **one application per user** even under racing requests.
- Every applicant query is scoped to session user ID and active account is revalidated by `ProtectedPagesFilter`.
- Admin list/detail/POST require refreshed ADMIN role. CSRF mandatory for state-changing forms.
- Review locks row `FOR UPDATE`, permits only `PENDING → APPROVED/REJECTED` and writes history in the same JDBC transaction.
- No upload of trade certificates/passports or financial records. Avoid real personal details in demonstration data.
- JSP uses `<c:out>` for untrusted text; fields are bounded and checked on server. Private note is not displayed on customer status page.
- The status APPROVED is only a marker of simulated administrator review. It never grants `user_roles`, wholesale prices, billing terms or privileges.

## Recovery

If Phase 9 cannot pass local tests, restore your pre-Phase9 project-folder backup. **Do not try to downgrade the database by deleting unrelated tables**; since V006 is additive, investigate and repair forward, or restore your *separate* verified MySQL backup in a controlled manner. Keep all test results.
