# MHAM AUTO Academic V1 Phase 1.3 smoke test. Run AFTER 'mvn clean package' and Tomcat deployment.
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$war = Join-Path $root 'target\mham-auto.war'
$base = 'http://localhost:8080/mham-auto'
function Assert-True([bool]$ok, [string]$message) {
    if (-not $ok) { throw "FAIL: $message" }
    Write-Host "PASS: $message" -ForegroundColor Green
}
Write-Host 'MHAM AUTO - Phase 1.3 validation' -ForegroundColor Cyan
Assert-True (Test-Path -LiteralPath $war) 'WAR exists'
Add-Type -AssemblyName System.IO.Compression.FileSystem
$archive = [System.IO.Compression.ZipFile]::OpenRead($war)
try {
    $names = @($archive.Entries | ForEach-Object { $_.FullName })
    foreach ($expected in @(
        'WEB-INF/classes/com/mhamauto/servlet/CatalogPreviewServlet.class',
        'WEB-INF/classes/com/mhamauto/service/CatalogPreviewService.class',
        'WEB-INF/views/catalog-preview.jsp',
        'assets/css/catalog-preview.css',
        'assets/js/catalog-preview.js',
        'assets/images/phase13/hero-assembly.svg'
    )) { Assert-True ($names -contains $expected) "WAR includes $expected" }
} finally { $archive.Dispose() }
$preview = Invoke-WebRequest -Uri "$base/catalog" -UseBasicParsing -TimeoutSec 15
Assert-True ($preview.StatusCode -eq 200) 'GET /catalog returns HTTP 200'
Assert-True ($preview.Content -match 'Parts, made easier') 'JSP category heading renders'
Assert-True ($preview.Content -match 'Engine Components') 'Servlet category data reaches JSP'
Assert-True ($preview.Content -match 'Parts for your next drive') 'Retail selection is rendered by Java'
$trade = Invoke-WebRequest -Uri "$base/catalog?audience=trade" -UseBasicParsing -TimeoutSec 15
Assert-True ($trade.Content -match 'Made for the workshop') 'Trade view switches dynamically'
Assert-True ($trade.Content -match 'Suspension &amp; Steering|Suspension & Steering') 'All category data is rendered'
$css = Invoke-WebRequest -Uri "$base/assets/css/catalog-preview.css" -UseBasicParsing -TimeoutSec 15
Assert-True ($css.StatusCode -eq 200 -and $css.Content -match 'mh-hero') 'Local CSS loads'
$js = Invoke-WebRequest -Uri "$base/assets/js/catalog-preview.js" -UseBasicParsing -TimeoutSec 15
Assert-True ($js.StatusCode -eq 200 -and $js.Content -match 'aria-expanded') 'Local JavaScript loads'
$svg = Invoke-WebRequest -Uri "$base/assets/images/phase13/hero-assembly.svg" -UseBasicParsing -TimeoutSec 15
Assert-True ($svg.StatusCode -eq 200 -and $svg.Content -match '<svg') 'Local SVG hero art loads'
$home = Invoke-WebRequest -Uri "$base/" -UseBasicParsing -TimeoutSec 15
Assert-True ($home.StatusCode -eq 200 -and $home.Content -match 'MHAM AUTO') 'Existing homepage still works'
$returned400 = $false
try { $null = Invoke-WebRequest -Uri "$base/catalog?audience=invalid" -UseBasicParsing -TimeoutSec 15 }
catch { if ($_.Exception.Response -and [int]$_.Exception.Response.StatusCode -eq 400) { $returned400 = $true } else { throw } }
Assert-True $returned400 'Invalid audience receives HTTP 400'
Write-Host 'PHASE 1.3 HTTP SMOKE TEST PASSED' -ForegroundColor Green
