param([switch]$SkipBuild,[switch]$SkipHttp)
$ErrorActionPreference='Stop'
$root=Split-Path -Parent $PSScriptRoot
Push-Location -LiteralPath $root
try {
  & powershell -NoProfile -ExecutionPolicy Bypass -File '.\scripts\Test-Phase7-Offline.ps1'
  if($LASTEXITCODE -ne 0){throw 'Offline checks failed'}
  if(-not $SkipBuild){
    & mvn clean package dependency:copy-dependencies '-DincludeScope=runtime' '-DoutputDirectory=target/dependency'
    if($LASTEXITCODE -ne 0){throw 'Maven build failed'}
  }
  if(-not $SkipHttp) {
    $base='http://localhost:8080/mham-auto'
    $guest=New-Object Microsoft.PowerShell.Commands.WebRequestSession
    $login=Invoke-WebRequest -UseBasicParsing -WebSession $guest -Uri "$base/orders" -TimeoutSec 25
    if($login.Content -notmatch 'Sign in' -and $login.Content -notmatch 'Login' -and $login.BaseResponse.ResponseUri.AbsoluteUri -notmatch '/login'){
      throw 'Unauthenticated orders access was not redirected to login'
    }
    $a=New-Object Microsoft.PowerShell.Commands.WebRequestSession
    $r=Invoke-WebRequest -UseBasicParsing -WebSession $a -Uri "$base/register" -TimeoutSec 25
    $token=[regex]::Match($r.Content,'name="csrf"\s+value="([^"]+)"')
    if(-not $token.Success){throw 'Registration form CSRF missing'}
    $email='phase7-'+[guid]::NewGuid().ToString('N')+'@example.test'
    $registered=Invoke-WebRequest -UseBasicParsing -Method Post -WebSession $a -Uri "$base/register" -Body @{csrf=$token.Groups[1].Value;name='Portal Test User';email=$email;password='Safe-test-password-2026!';confirm='Safe-test-password-2026!'} -TimeoutSec 25
    if($registered.Content -notmatch 'CUSTOMER DASHBOARD'){throw 'Customer dashboard did not render'}
    $orders=Invoke-WebRequest -UseBasicParsing -Uri "$base/orders" -WebSession $a -TimeoutSec 25
    if($orders.Content -notmatch 'No demo orders yet'){throw 'Empty order history not shown'}
    $account=Invoke-WebRequest -UseBasicParsing -WebSession $a -Uri "$base/account" -TimeoutSec 25
    $csrf=[regex]::Match($account.Content,'name="csrf"\s+value="([^"]+)"')
    if(-not $csrf.Success){throw 'Account CSRF missing'}
    try {Invoke-WebRequest -UseBasicParsing -WebSession $a -Method Post -Uri "$base/account" -Body @{csrf='invalid';action='updateName';displayName='Changed'} -TimeoutSec 25 | Out-Null;throw 'Invalid CSRF accepted'}
    catch {if(-not $_.Exception.Response -or [int]$_.Exception.Response.StatusCode -ne 403){throw}}
    $changed=Invoke-WebRequest -UseBasicParsing -WebSession $a -Method Post -Uri "$base/account" -Body @{csrf=$csrf.Groups[1].Value;action='updateName';displayName='Renamed Phase Seven'} -TimeoutSec 25
    if($changed.Content -notmatch 'Renamed Phase Seven'){throw 'Profile name update not displayed'}
    function Expect-HttpError($uri,$expected,$session) {
      try {Invoke-WebRequest -UseBasicParsing -Uri $uri -WebSession $session -TimeoutSec 25 | Out-Null;throw "Expected HTTP $expected from $uri"}
      catch {if(-not $_.Exception.Response -or [int]$_.Exception.Response.StatusCode -ne $expected){throw}}
    }
    Expect-HttpError "$base/orders?page=0" 400 $a
    $ref='DEMO-0123456789ABCDEFABCD'
    Expect-HttpError "$base/orders/view?ref=$ref" 404 $a
    Expect-HttpError "$base/orders/view?ref=bad" 400 $a
    Write-Host "PASS Phase 7 HTTP: guest isolation, dashboard, empty history, profile, CSRF, invalid page/reference" -ForegroundColor Green
    Write-Host "Temporary test account: $email"
  }
  Write-Host 'PASS Phase 7 verification' -ForegroundColor Green
} finally {Pop-Location}
