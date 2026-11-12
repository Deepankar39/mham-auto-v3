# MHAM AUTO — Phase 4 implementation and local acceptance

**Scope:** Customer registration/login/logout; password hashing, role-based sessions, account dashboard and a **read-only** admin-only entry route. **Not** Phase 5 cart, Phase 7 order history or Phase 8 admin CRUD.

## Preflight

- You have installed and locally tested Phases 1.3, 1.4, 2 and 3 in `D:\Java Project\MHAM AUTO`.
- Java 21, Maven 3.9.11, Tomcat 10.1.61, MySQL 8.x, and Phase 2 DB credentials are available.
- This is an **incremental overlay**: keep existing working files; it never intentionally deletes source/catalogue data.
- If you have locally modified files that the manifest lists as overwritten, compare/backup first.
- Keep the real database password out of project files, archives and screenshots.

## 1. Backup and extract in PowerShell

```powershell
$root = 'D:\Java Project\MHAM AUTO'
$zip = "$env:USERPROFILE\Downloads\MHAM_AUTO_Phase4_Authentication_Overlay.zip"
$backup = 'D:\Java Project\MHAM_AUTO_PrePhase4_' + (Get-Date -Format yyyyMMdd_HHmmss)
Copy-Item -LiteralPath $root -Destination $backup -Recurse -Force
Expand-Archive -LiteralPath $zip -DestinationPath $root -Force
Set-Location -LiteralPath $root
Write-Host "Backup saved: $backup"
```

## 2. Database credentials

Use the **same** `mham_auto` database/credentials you used for Phase 2 and Phase 3. To set environment variables for the current PowerShell session (skip if they are already configured):

```powershell
$env:MHAM_DB_USER = 'mham_app'
$secret = Read-Host 'MySQL password' -AsSecureString
$pointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secret)
try { $env:MHAM_DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($pointer) }
finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($pointer) }
Remove-Variable secret,pointer
```

`MHAM_DB_URL` may remain at the Phase 2 default. These variables are inherited by `java` and newly launched Tomcat scripts from the same terminal; a Windows Service-based Tomcat requires credentials configured for the service environment separately.

## 3. Offline tests, Maven and migration

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File '.\scripts\Test-Phase4-Offline.ps1'
if ($LASTEXITCODE -ne 0) { throw 'Offline checks failed' }
mvn clean package dependency:copy-dependencies '-DincludeScope=runtime' '-DoutputDirectory=target/dependency'
if ($LASTEXITCODE -ne 0) { throw 'Maven or JUnit checks failed' }
$cp = 'target/classes;target/dependency/*'
java -cp $cp com.mhamauto.cli.IdentityCli migrate
if ($LASTEXITCODE -ne 0) { throw 'V002 schema migration failed' }
java -cp $cp com.mhamauto.cli.IdentityCli migrate
if ($LASTEXITCODE -ne 0) { throw 'Migration re-run failed' }
java -cp $cp com.mhamauto.cli.IdentityCli status
if ($LASTEXITCODE -ne 0) { throw 'Identity schema check failed' }
```

The first migration creates **three** new identity tables: `users`, `roles`, and `user_roles`, and seeds `CUSTOMER` and `ADMIN` roles. `schema_migrations` records V002 and its exact file checksum. MySQL DDL may commit partially if interrupted: restart with the **same** V002 file. Never modify applied SQL or clear existing tables to retry.

To inspect roles in MySQL Workbench, run `database/04_phase4_validation.sql` (read-only). Database-managed admin access is separate from the web app, and a browser cannot grant itself a role.

## 4. Deploy the new WAR

Do this from the terminal with the DB environment variables configured:

```powershell
$tomcat = 'D:\Java Tools\apache-tomcat-10.1.61'
& "$tomcat\bin\shutdown.bat"
Start-Sleep -Seconds 4
Copy-Item -LiteralPath '.\target\mham-auto.war' -Destination "$tomcat\webapps\mham-auto.war" -Force
& "$tomcat\bin\startup.bat"
```

After Tomcat deploys, browse:

- `http://localhost:8080/mham-auto/` — original homepage
- `http://localhost:8080/mham-auto/catalog` — original preview
- `http://localhost:8080/mham-auto/products` — Phase 3 MySQL catalogue
- `http://localhost:8080/mham-auto/register` — create a customer account
- `http://localhost:8080/mham-auto/login` — login
- `http://localhost:8080/mham-auto/account` — logged-in customer area
- `http://localhost:8080/mham-auto/admin` — **ADMIN-only read-only entry**

If Tomcat still shows an old WAR, shut down cleanly and remove **only** the old extracted `webapps/mham-auto` directory, then restart Tomcat with the updated WAR. Do **not** delete `mham_auto` MySQL, the project tree, or your backups.

## 5. Live smoke/regression checks

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File '.\scripts\Test-Phase4.ps1' -SkipBuild
if ($LASTEXITCODE -ne 0) { throw 'Phase 4 smoke tests failed' }
```

This creates one uniquely named **test** account in MySQL, checks normal login/logout, blocked `/admin` access, invalid CSRF rejection, and previous offline tests. A successful HTTP response alone is insufficient: the smoke test checks content after redirects.

Manual browser checklist:

1. Register a new user, enter invalid or mismatched passwords, and confirm server-side form errors are handled safely.
2. Register successfully, then confirm `/account` displays your name and email.
3. Open `/admin` using that account: expect HTTP **403**.
4. Logout (POST via form), then open `/account`: expect redirect to `/login`.
5. Log in again. Refresh to confirm the session persists, and close/reopen a private browser session to confirm it does not remain signed in.
6. Confirm old `/`, `/catalog`, `/products`, `/product?handle=...`, `/health` and `/api/db-status` features continue working.

## 6. Optional local administrator bootstrap

Only after registering an account, promote **that account** from the developer machine, not from a browser:

```powershell
$cp = 'target/classes;target/dependency/*'
java -cp $cp com.mhamauto.cli.IdentityCli grant-admin 'your-registered-email@example.com' --confirm-local-admin-bootstrap
if ($LASTEXITCODE -ne 0) { throw 'Admin role grant failed' }
```

Sign out and sign in again, then visit `/admin`. This page verifies role enforcement; it **does not** contain product management yet. Never promote customer accounts unintentionally. No default/admin backdoor password is shipped.

## Security constraints and known limits

- Passwords: PBKDF2-HMAC-SHA256, per-account 128-bit random salt, 600,000 iterations, constant-time byte comparison. No plaintext secrets in database.
- Cookies: 30-minute idle session, HttpOnly, cookie-only tracking, Tomcat SameSite=Lax. The cookie's Secure flag requires HTTPS in production; localhost HTTP is for academic testing only.
- Sessions: existing ID is invalidated and replaced after successful authentication. State-changing identity requests validate synchronizer CSRF tokens; logout is POST only.
- Access: `/account` is authenticated, `/admin` is ADMIN-only; user status/roles are rechecked from MySQL on each protected request. Registration always grants CUSTOMER.
- Throttle: 6 unsuccessful login attempts per IP/email window over 15 minutes **per application instance**. It is not distributed-rate-limiting. Production also needs edge controls, HTTPS, admin MFA, security testing, password recovery and verified email ownership.
- No permanent blocking/account lockout is implemented for this academic demonstration. A strong brute-force defense across processes and networks is a production enhancement.
- Do not launch the application on the public Internet as-is. It is an academic demonstration.

## Definition of Done

- Phase 4 JUnit, plain-Java, Maven and SQL status checks pass locally.
- Customer account registration/authentication persists in MySQL, no role escalation through registration.
- CSRF, session fixation, logout and protected-route negative cases pass.
- `/admin` denies regular users and allows only explicitly bootstrapped ADMIN users.
- Phases 1.3–3 remain working. Provide terminal output and any errors before beginning Phase 5.
