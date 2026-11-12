param([switch]$SkipBuild, [string]$BaseUrl = 'http://localhost:8080/mham-auto')
$ErrorActionPreference = 'Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
if (-not $SkipBuild) {
  & mvn -q clean test package
  if ($LASTEXITCODE -ne 0) { throw 'Maven test/package failed' }
}
foreach ($route in @('/','/catalog','/products','/login','/register','/health')) {
  $r = Invoke-WebRequest -Uri ($BaseUrl + $route) -MaximumRedirection 5 -UseBasicParsing
  if ([int]$r.StatusCode -ne 200) { throw "Expected HTTP 200 for $route" }
  Write-Host "PASS $route" -ForegroundColor Green
}
# Authenticated pages must not expose private content to an anonymous visitor.
foreach ($route in @('/trade/apply','/trade/status','/admin/trade-applications','/admin/trade-application?id=1')) {
  $code = 0
  try {
    $response = Invoke-WebRequest -Uri ($BaseUrl + $route) -MaximumRedirection 0 -UseBasicParsing -ErrorAction Stop
    $code = [int]$response.StatusCode
  } catch {
    if ($_.Exception.Response) { $code = [int]$_.Exception.Response.StatusCode }
    else { throw }
  }
  if ($code -notin @(302,303,401,403)) { throw "Anonymous route $route unexpectedly returned $code" }
  Write-Host "PASS anonymous protection $route ($code)" -ForegroundColor Green
}
Write-Host 'PASS public and anonymous HTTP checks.' -ForegroundColor Green
Write-Host 'MANUAL: authenticated submission, persistence, ADMIN review, CSRF, isolation and audit events still require testing.'
