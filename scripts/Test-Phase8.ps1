param([switch]$SkipBuild, [string]$BaseUrl = 'http://localhost:8080/mham-auto')
$ErrorActionPreference = 'Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
if (-not $SkipBuild) {
  & mvn -q test package
  if ($LASTEXITCODE -ne 0) { throw 'Maven build failed' }
}
$paths = @('/', '/catalog', '/products', '/login', '/register', '/health')
foreach ($route in $paths) {
  $response = Invoke-WebRequest -Uri ($BaseUrl + $route) -MaximumRedirection 5 -UseBasicParsing
  if ([int]$response.StatusCode -ne 200) { throw "Route $route returned $($response.StatusCode)" }
  Write-Host "PASS $route" -ForegroundColor Green
}
# Anonymous users MUST NOT see an admin dashboard. Disable auto redirect and accept only 302/303.
$adminUrl = $BaseUrl + '/admin'
try {
  $r = Invoke-WebRequest -Uri $adminUrl -MaximumRedirection 0 -UseBasicParsing -ErrorAction Stop
  if ([int]$r.StatusCode -ne 302 -and [int]$r.StatusCode -ne 303) { throw 'Anonymous admin was not redirected to login' }
} catch {
  $status = $_.Exception.Response.StatusCode.value__
  if ($status -ne 302 -and $status -ne 303) { throw "Admin access smoke test failed: $($_.Exception.Message)" }
}
Write-Host 'PASS anonymous admin protection' -ForegroundColor Green
Write-Host 'Live public checks passed. Manual authenticated admin CRUD/order tests are still required.'
