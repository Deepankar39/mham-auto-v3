# Execute after WAR build and deployment to a running Tomcat 10.1 instance.
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$base = 'http://localhost:8080/mham-auto'
$war = Join-Path $root 'target\mham-auto.war'
function Require([bool]$condition, [string]$message) {
    if (-not $condition) { throw "FAIL: $message" }
    Write-Host "PASS: $message" -ForegroundColor Green
}
Require (Test-Path -LiteralPath $war) 'WAR exists'
Add-Type -AssemblyName System.IO.Compression.FileSystem
$archive = [System.IO.Compression.ZipFile]::OpenRead($war)
try {
    $names = @($archive.Entries | ForEach-Object FullName)
    foreach ($expected in @(
        'WEB-INF/classes/com/mhamauto/servlet/HealthServlet.class',
        'WEB-INF/classes/com/mhamauto/config/AppBootstrapListener.class',
        'WEB-INF/classes/com/mhamauto/filter/RequestDefaultsFilter.class',
        'WEB-INF/classes/com/mhamauto/media/ExternalImageUrlPolicy.class',
        'WEB-INF/classes/mham-auto.properties',
        'WEB-INF/views/fragments/catalog-header.jspf',
        'WEB-INF/views/fragments/document-basics.jspf',
        'WEB-INF/views/error.jsp',
        'WEB-INF/web.xml',
        'assets/css/error.css'
    )) { Require ($names -contains $expected) "WAR contains $expected" }
} finally { $archive.Dispose() }
$health = Invoke-WebRequest -Uri "$base/health" -UseBasicParsing -TimeoutSec 15
Require ($health.StatusCode -eq 200 -and $health.Content -match 'OK - MHAM AUTO Academic V1') '/health liveness'
Require ($health.Headers['X-Content-Type-Options'] -eq 'nosniff') 'Common nosniff header'
Require ($health.Headers['X-Frame-Options'] -eq 'DENY') 'Common frame protection header'
$home = Invoke-WebRequest -Uri "$base/" -UseBasicParsing -TimeoutSec 15
Require ($home.StatusCode -eq 200 -and $home.Content -match '/catalog') 'Homepage links to catalogue'
$missing404 = $false
try { $null = Invoke-WebRequest -Uri "$base/not-a-real-route-phase14" -UseBasicParsing -TimeoutSec 15 }
catch {
    $res = $_.Exception.Response
    if ($res -and [int]$res.StatusCode -eq 404) { $missing404 = $true } else { throw }
}
Require $missing404 'Missing page returns 404'
& (Join-Path $PSScriptRoot 'Test-Phase1_3.ps1')
if (-not $?) { throw 'Phase 1.3 regression script failed' }
Write-Host 'PHASE 1.4 LIVE TESTS PASSED' -ForegroundColor Green
