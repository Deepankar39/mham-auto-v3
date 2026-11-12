param([string]$ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path)
$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath $ProjectRoot
$required = @(
 'src/main/java/com/mhamauto/web/RequestSecurity.java',
 'src/main/java/com/mhamauto/filter/RequestDefaultsFilter.java',
 'src/main/webapp/assets/css/phase10.css',
 'src/main/webapp/assets/js/phase10.js',
 'src/main/webapp/WEB-INF/views/fragments/document-basics.jspf',
 'src/main/webapp/WEB-INF/views/products.jsp',
 'src/main/webapp/WEB-INF/views/admin-products.jsp',
 'src/main/webapp/WEB-INF/views/trade-apply.jsp',
 'tests/Phase10OfflineCheck.java')
foreach ($file in $required) { if (-not (Test-Path -LiteralPath $file)) { throw "Missing Phase 10 file: $file" } }
$work = Join-Path 'target' 'phase10-offline'
New-Item -ItemType Directory -Path $work -Force | Out-Null
& javac --release 21 -encoding UTF-8 -d $work 'src/main/java/com/mhamauto/web/RequestSecurity.java' 'tests/Phase10OfflineCheck.java'
if ($LASTEXITCODE -ne 0) { throw 'Phase 10 javac offline compilation failed' }
& java -cp $work Phase10OfflineCheck
if ($LASTEXITCODE -ne 0) { throw 'Phase 10 origin and CSP rule assertions failed' }
$pages = Get-ChildItem -Path 'src/main/webapp' -Include '*.jsp','*.jspf' -Recurse -File
$inlines = @($pages | Select-String -Pattern 'style\s*=\s*["'']|on(click|error|load)\s*=\s*["'']' -CaseSensitive:$false)
if ($inlines.Count -gt 0) { throw 'Inline HTML styles/handlers detected; strict Content-Security-Policy might block them' }
$nav = Get-Content 'src/main/webapp/WEB-INF/views/fragments/catalog-header.jspf' -Raw
if ($nav -match 'aria-current="page"') { throw 'Catalogue navigation has a hardcoded current-page marker' }
if (Get-Command node -ErrorAction SilentlyContinue) {
 foreach($js in @('src/main/webapp/assets/js/phase10.js','src/main/webapp/assets/js/catalog-preview.js','src/main/webapp/assets/js/cart-badge.js')) {
   & node --check $js
   if ($LASTEXITCODE -ne 0) { throw "Invalid JavaScript syntax in $js" }
 }
} else { Write-Host 'Node.js not found, skipping optional JavaScript syntax check.' -ForegroundColor Yellow }
Write-Host 'PASS - Phase 10 offline checks' -ForegroundColor Green
