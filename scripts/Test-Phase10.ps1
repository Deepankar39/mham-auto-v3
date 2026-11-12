param([string]$BaseUrl = 'http://localhost:8080/mham-auto', [switch]$SkipBuild)
$ErrorActionPreference = 'Stop'
$base = $BaseUrl.TrimEnd('/')
function Fetch($path, $method='GET', $origin=$null) {
    $params = @{ Uri=($base+$path); Method=$method; TimeoutSec=20; UseBasicParsing=$true; MaximumRedirection=0 }
    if ($null -ne $origin) { $params.Headers = @{ Origin=$origin } }
    try { return Invoke-WebRequest @params }
    catch [System.Net.WebException] {
        if ($_.Exception.Response) { return $_.Exception.Response }
        throw
    }
}
function AssertStatus($path,$expected,$method='GET',$origin=$null) {
    $r = Fetch $path $method $origin
    $status = [int]$r.StatusCode
    if ($status -ne $expected) { throw "Expected HTTP $expected for $method $path; received $status" }
    Write-Host "PASS $method $path => $status"
    return $r
}
if (-not $SkipBuild) {
  & powershell -NoProfile -ExecutionPolicy Bypass -File (Join-Path $PSScriptRoot 'Test-Phase10-Offline.ps1')
  if ($LASTEXITCODE -ne 0) { throw 'Offline verification failed' }
  & mvn test package
  if ($LASTEXITCODE -ne 0) { throw 'Maven/JUnit build failed' }
}
$homepage = AssertStatus '/' 200
if ($homepage.Headers['Content-Security-Policy'] -notmatch 'frame-ancestors') { throw 'Content-Security-Policy missing' }
if ($homepage.Headers['X-Content-Type-Options'] -ne 'nosniff') { throw 'nosniff missing' }
if ($homepage.Headers['Permissions-Policy'] -notmatch 'payment=') { throw 'Permissions-Policy missing' }
[void](AssertStatus '/catalog' 200)
[void](AssertStatus '/products' 200)
[void](AssertStatus '/health' 200)
[void](AssertStatus '/api/db-status' 200)
$asset=AssertStatus '/assets/css/phase10.css' 200
if ($asset.Headers['Cache-Control'] -notmatch 'max-age=300') { throw 'Asset cache header missing' }
[void](AssertStatus '/login' 200)
[void](AssertStatus '/register' 200)
[void](AssertStatus '/account' 302)
[void](AssertStatus '/admin' 302)
[void](AssertStatus '/trade/apply' 302)
[void](AssertStatus '/login' 403 'POST' 'http://untrusted.example')
Write-Host 'PASS - Phase 10 live HTTP smoke tests' -ForegroundColor Green
