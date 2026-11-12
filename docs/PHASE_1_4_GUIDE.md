# MHAM AUTO — Academic V1 / Phase 1.4

Status: supplied for **local verification**, not yet confirmed on the owner's Windows/Tomcat system.

## Purpose
This is a source **overlay**, not a replacement project. It builds on the already-working Phase 1.3. No new Maven dependency, database connection, login, cart, payment or CSV import is enabled.

## What changed
- Common `document-basics.jspf` is now included by the homepage and catalogue JSP.
- Page-family-specific header/footer fragments preserve existing design and markup. The homepage now includes a non-disruptive hero link to `/catalog`.
- The catalogue Servlet forwards through `ViewPaths.CATALOG_PREVIEW` but still uses the original service and six categories.
- `AppSettings` loads local-safe classpath properties (`KW`, `KWD`, demo mode). Invalid configuration fails application startup rather than silently enabling live features.
- `AppBootstrapListener` supplies the configuration to the Servlet context.
- `RequestDefaultsFilter` enforces UTF-8 defaults and basic security response headers. No authentication or CSRF claims are made in this phase.
- `/health` is an application-liveness endpoint; it does **not** check a MySQL connection.
- Custom 404/500 JSP, with a new small CSS asset; HTTP status codes remain correct.
- `ExternalImageUrlPolicy` validates Shopify CDN HTTPS URL references without fetching any image. Phase 2 will integrate CSV parsing/DB storage; Phase 3 will render real images.
- Restored the missing `tests/phase-1.3/CatalogPreviewServiceCheck.java`, which the existing Phase 1.3 PowerShell offline script already expects.

## Pre-extraction safety
1. Review the ZIP manifest in `docs/PHASE_1_4_MANIFEST.md` and back up your working project.
2. Stop changing the project while extracting the overlay.
3. Extract **into** `D:\Java Project\MHAM AUTO`, merging folders. Never create an extra nested project directory.
4. Source files not listed in the manifest must remain untouched. Existing `pom.xml` and `assets/css/style.css` / `catalog-preview.css`, JavaScript and SVGs are **not** replaced.
5. Do not put database passwords or `.env` secrets inside the project ZIP.

## PowerShell — back up and apply overlay
Change `$zip` to the actual path where you saved the downloaded ZIP.

```powershell
$project = 'D:\Java Project\MHAM AUTO'
$tomcat = 'D:\Java Tools\apache-tomcat-10.1.61'
$zip = Join-Path $env:USERPROFILE 'Downloads\MHAM_AUTO_Phase1_4_Overlay.zip'
$backup = 'D:\Java Project\MHAM_AUTO_Backup_Pre_Phase1_4_' + (Get-Date -Format 'yyyyMMdd_HHmmss')
if (-not (Test-Path -LiteralPath $zip)) { throw "ZIP not found: $zip" }
if (-not (Test-Path -LiteralPath (Join-Path $project 'pom.xml'))) { throw 'Wrong project folder' }
Copy-Item -LiteralPath $project -Destination $backup -Recurse -Force
Write-Host "Backup saved at: $backup"
Expand-Archive -LiteralPath $zip -DestinationPath $project -Force
Set-Location -LiteralPath $project
```

## PowerShell — build & offline regression

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File '.\scripts\Test-Phase1_3_Offline.ps1'
powershell -NoProfile -ExecutionPolicy Bypass -File '.\scripts\Test-Phase1_4_Offline.ps1'
mvn clean package
if ($LASTEXITCODE -ne 0) { throw 'Maven build failed' }
Test-Path -LiteralPath '.\target\mham-auto.war'
```

## PowerShell — deploy & live regression

```powershell
Copy-Item -LiteralPath '.\target\mham-auto.war' -Destination (Join-Path $tomcat 'webapps\mham-auto.war') -Force
Start-Sleep -Seconds 10
powershell -NoProfile -ExecutionPolicy Bypass -File '.\scripts\Test-Phase1_4.ps1'
Start-Process 'http://localhost:8080/mham-auto/'
Start-Process 'http://localhost:8080/mham-auto/catalog'
Start-Process 'http://localhost:8080/mham-auto/catalog?audience=trade'
Start-Process 'http://localhost:8080/mham-auto/health'
```

Tomcat must already be running. If Tomcat does not pick up a replaced WAR, restart it after deployment and wait for logs to show a completed deployment. Do not remove any exploded application folder while Tomcat is running.

## Acceptance checklist
- [ ] Offline Phase 1.3 + 1.4 checks pass
- [ ] Maven WAR build passes
- [ ] Homepage displays and links to preview
- [ ] Retail and Trade catalogue continue rendering six original categories
- [ ] CSS, JS and SVGs still load
- [ ] Invalid `audience` returns HTTP 400
- [ ] `/health` returns HTTP 200 and required headers
- [ ] Unknown route returns HTTP 404
- [ ] Error view and navigation look correct on mobile

Stop after these checks and share results. Phase 2 database/JDBC/CSV importer is next only after confirmation.
