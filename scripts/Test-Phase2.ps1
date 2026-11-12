param([switch]$SkipHttp)
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
Push-Location -LiteralPath $root
try {
  if (-not $env:MHAM_DB_USER -or -not $env:MHAM_DB_PASSWORD) {
    throw 'Set MHAM_DB_USER and MHAM_DB_PASSWORD in this PowerShell session before running this script'
  }
  & powershell -NoProfile -ExecutionPolicy Bypass -File '.\scripts\Test-Phase2-Offline.ps1'
  if ($LASTEXITCODE -ne 0) { throw 'Offline regression failed' }
  & mvn -q clean package dependency:copy-dependencies '-DincludeScope=runtime' '-DoutputDirectory=target/dependency'
  if ($LASTEXITCODE -ne 0) { throw 'Maven tests/build/dependency staging failed' }
  $classpath = 'target/classes;target/dependency/*'
  $sample = Join-Path $root 'samples\product data sample.csv'
  & java -cp $classpath com.mhamauto.cli.CatalogCli dry-run "--file=$sample"
  if ($LASTEXITCODE -ne 0) { throw 'CSV dry-run rejected sample' }
  & java -cp $classpath com.mhamauto.cli.CatalogCli migrate
  if ($LASTEXITCODE -ne 0) { throw 'Schema migration failed' }
  & java -cp $classpath com.mhamauto.cli.CatalogCli import "--file=$sample"
  if ($LASTEXITCODE -ne 0) { throw 'First sample import failed' }
  $first = & java -cp $classpath com.mhamauto.cli.CatalogCli stats
  if ($LASTEXITCODE -ne 0) { throw 'DB statistics failed' }
  & java -cp $classpath com.mhamauto.cli.CatalogCli import "--file=$sample"
  if ($LASTEXITCODE -ne 0) { throw 'Repeat import failed' }
  $second = & java -cp $classpath com.mhamauto.cli.CatalogCli stats
  if ($LASTEXITCODE -ne 0) { throw 'Second DB statistics failed' }
  $names=@('products','product_variants','media_assets','product_images','variant_images','inventory','unverified_prices')
  foreach ($name in $names) {
    $l1=($first | Where-Object { $_ -match "^$name\s+\d+" } | Select-Object -First 1)
    $l2=($second | Where-Object { $_ -match "^$name\s+\d+" } | Select-Object -First 1)
    if (-not $l1 -or -not $l2 -or ($l1 -replace '\s+',' ') -ne ($l2 -replace '\s+',' ')) {
      throw "Idempotence failure in $name (first=$l1 second=$l2)"
    }
  }
  if (-not $SkipHttp) {
    $response=Invoke-WebRequest -Uri 'http://localhost:8080/mham-auto/api/db-status' -UseBasicParsing -TimeoutSec 15
    if ($response.StatusCode -ne 200 -or $response.Content -notmatch 'DB READY') { throw 'DB readiness endpoint failed' }
  }
  Write-Host 'PASS: Phase 2 Maven tests, CSV parsing, migration, repeat-import, and readiness checks' -ForegroundColor Green
} finally { Pop-Location }
