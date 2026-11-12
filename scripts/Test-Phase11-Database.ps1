$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath (Split-Path $PSScriptRoot -Parent)
foreach($k in @('MHAM_DB_USER','MHAM_DB_PASSWORD')) {
  if ([string]::IsNullOrWhiteSpace([Environment]::GetEnvironmentVariable($k))) { throw "Missing $k in current session" }
}
if (!(Test-Path 'target/classes')) { throw 'Run Maven build before the database gate' }
if (!(Test-Path 'target/dependency')) { throw 'Run Maven dependency:copy-dependencies before the database gate' }
$cp = 'target/classes;target/dependency/*'
$commands = @(
 @('com.mhamauto.cli.CatalogCli','stats'),
 @('com.mhamauto.cli.IdentityCli','status'),
 @('com.mhamauto.cli.CartCli','status'),
 @('com.mhamauto.cli.CheckoutCli','status'),
 @('com.mhamauto.cli.AdminCli','status'),
 @('com.mhamauto.cli.TradeCli','status')
)
foreach ($commandSpec in $commands) {
 Write-Host "CHECK $($commandSpec[0]) $($commandSpec[1])" -ForegroundColor Cyan
 & java -cp $cp $commandSpec[0] $commandSpec[1]
 if ($LASTEXITCODE -ne 0) { throw "Database check failed for $($commandSpec[0])" }
}
Write-Host 'PASS six read-only database status checks' -ForegroundColor Green
Write-Host 'Next: run database/15_phase11_read_only_validation.sql in MySQL Workbench and attach the output.'
