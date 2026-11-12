param([switch]$SkipBuild, [switch]$SkipHttp)
$ErrorActionPreference='Stop'
$root=Split-Path -Parent $PSScriptRoot
Push-Location -LiteralPath $root
try {
  if (-not $env:MHAM_DB_USER -or -not $env:MHAM_DB_PASSWORD) {throw 'Start in a PowerShell session with MHAM_DB_USER and MHAM_DB_PASSWORD configured'}
  & powershell -NoProfile -ExecutionPolicy Bypass -File '.\scripts\Test-Phase3-Offline.ps1'
  if($LASTEXITCODE -ne 0) {throw 'Offline regression failed'}
  if(-not $SkipBuild) {
    & mvn clean package dependency:copy-dependencies '-DincludeScope=runtime' '-DoutputDirectory=target/dependency'
    if($LASTEXITCODE -ne 0) {throw 'Maven Phase 3 build failed'}
  }
  $cp='target/classes;target/dependency/*'
  $result = & java -cp $cp com.mhamauto.cli.CatalogReadCli 4
  if($LASTEXITCODE -ne 0) {throw 'Phase 3 DB read-only smoke failed'}
  $result | ForEach-Object {Write-Host $_}
  if(-not $SkipHttp) {
    $base='http://localhost:8080/mham-auto'
    $listing=Invoke-WebRequest -UseBasicParsing -TimeoutSec 20 -Uri "$base/products"
    if($listing.StatusCode -ne 200 -or $listing.Content -notmatch 'Product catalogue' -or $listing.Content -notmatch 'p3-card') {throw '/products failed'}
    $query=Invoke-WebRequest -UseBasicParsing -TimeoutSec 20 -Uri "$base/products?q=wheel"
    if($query.StatusCode -ne 200 -or $query.Content -notmatch 'Product catalogue') {throw 'Product search failed'}
    $handle=($result | Where-Object {$_ -match '^PRODUCT_HANDLE='} | Select-Object -First 1) -replace '^PRODUCT_HANDLE=',''
    if(-not $handle) {throw 'DB smoke did not print a product handle'}
    $detail=Invoke-WebRequest -UseBasicParsing -TimeoutSec 20 -Uri "$base/product?handle=$([uri]::EscapeDataString($handle))"
    if($detail.StatusCode -ne 200 -or $detail.Content -notmatch 'Variants and source data') {throw '/product detail failed'}
    foreach($uri in @("$base/product?handle=nonexistent-mham-phase3-product", "$base/products?sort=drop-table", "$base/products?page=10001")) {
      $expected = if($uri -match 'nonexistent') {404} else {400}
      try {Invoke-WebRequest -UseBasicParsing -TimeoutSec 15 -Uri $uri | Out-Null;throw "Expected HTTP $expected: $uri"}
      catch {if(-not $_.Exception.Response -or [int]$_.Exception.Response.StatusCode -ne $expected){throw}}
    }
    & powershell -NoProfile -ExecutionPolicy Bypass -File '.\scripts\Test-Phase1_3.ps1'
    if($LASTEXITCODE -ne 0) {throw 'Original Phase 1.3 catalogue regression failed'}
    $home=Invoke-WebRequest -UseBasicParsing -TimeoutSec 15 -Uri "$base/"
    if($home.Content -notmatch '/products') {throw 'Homepage missing live catalogue link'}
  }
  Write-Host 'PASS: Phase 3 build, database catalogue queries, product details, filters and legacy preview checks' -ForegroundColor Green
} finally {Pop-Location}
