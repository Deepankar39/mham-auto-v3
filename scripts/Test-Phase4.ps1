param([switch]$SkipBuild,[switch]$SkipHttp)
$ErrorActionPreference='Stop'
$root=Split-Path -Parent $PSScriptRoot
Push-Location -LiteralPath $root
try {
  if(-not $env:MHAM_DB_USER -or -not $env:MHAM_DB_PASSWORD) {throw 'Set MHAM_DB_USER and MHAM_DB_PASSWORD in this PowerShell session'}
  & powershell -NoProfile -ExecutionPolicy Bypass -File '.\scripts\Test-Phase4-Offline.ps1'
  if($LASTEXITCODE -ne 0){throw 'Phase 4 offline suite failed'}
  if(-not $SkipBuild){
    & mvn clean package dependency:copy-dependencies '-DincludeScope=runtime' '-DoutputDirectory=target/dependency'
    if($LASTEXITCODE -ne 0){throw 'Maven build failed'}
  }
  $cp='target/classes;target/dependency/*'
  & java -cp $cp com.mhamauto.cli.IdentityCli status
  if($LASTEXITCODE -ne 0){throw 'Identity database smoke failed; migrate V002 first'}
  if(-not $SkipHttp){
    $base='http://localhost:8080/mham-auto'
    $session=New-Object Microsoft.PowerShell.Commands.WebRequestSession
    $register=Invoke-WebRequest -UseBasicParsing -Uri "$base/register" -WebSession $session -TimeoutSec 20
    if($register.StatusCode -ne 200 -or $register.Content -notmatch 'Create your'){throw 'GET registration failed'}
    $csrfMatch=[regex]::Match($register.Content,'name="csrf"\s+value="([^"]+)"')
    if(-not $csrfMatch.Success){throw 'Missing CSRF token'}
    $email='phase4-smoke-'+[guid]::NewGuid().ToString('N')+'@example.test'
    $form=@{csrf=$csrfMatch.Groups[1].Value;name='Phase Four Test Customer';email=$email;password='Test-account-password-2026!';confirm='Test-account-password-2026!'}
    $account=Invoke-WebRequest -UseBasicParsing -Uri "$base/register" -Method Post -Body $form -WebSession $session -TimeoutSec 30
    if($account.StatusCode -ne 200 -or $account.Content -notmatch 'Your customer account'){throw 'Registration/account workflow failed'}
    try {
      Invoke-WebRequest -UseBasicParsing -Uri "$base/admin" -WebSession $session -TimeoutSec 20 | Out-Null
      throw 'Regular customer incorrectly accessed /admin'
    } catch {
      if(-not $_.Exception.Response -or [int]$_.Exception.Response.StatusCode -ne 403){throw}
    }
    $badSession=New-Object Microsoft.PowerShell.Commands.WebRequestSession
    try {
      Invoke-WebRequest -UseBasicParsing -Uri "$base/login" -Method Post -Body @{email=$email;password='Wrong-password';csrf='INVALID'} -WebSession $badSession -TimeoutSec 20 | Out-Null
      throw 'Missing/invalid CSRF was accepted'
    } catch {
      if(-not $_.Exception.Response -or [int]$_.Exception.Response.StatusCode -ne 403){throw}
    }
    $logoutToken=[regex]::Match($account.Content,'name="csrf"\s+value="([^"]+)"')
    if(-not $logoutToken.Success){throw 'Missing account logout CSRF token'}
    $loggedOut=Invoke-WebRequest -UseBasicParsing -Uri "$base/logout" -Method Post -Body @{csrf=$logoutToken.Groups[1].Value} -WebSession $session -TimeoutSec 20
    if($loggedOut.Content -notmatch 'Welcome back'){throw 'Logout did not return to login'}
    $private=Invoke-WebRequest -UseBasicParsing -Uri "$base/account" -WebSession $session -TimeoutSec 20
    if($private.Content -notmatch 'Welcome back'){throw 'Logout did not revoke account session'}
    $loginToken=[regex]::Match($private.Content,'name="csrf"\s+value="([^"]+)"')
    if(-not $loginToken.Success){throw 'Missing login token'}
    $loggedIn=Invoke-WebRequest -UseBasicParsing -Uri "$base/login" -Method Post -Body @{email=$email;password='Test-account-password-2026!';csrf=$loginToken.Groups[1].Value} -WebSession $session -TimeoutSec 30
    if($loggedIn.Content -notmatch 'Your customer account'){throw 'Login after logout failed'}
    Write-Host 'PASS: Phase 4 registration, login, logout, CSRF, account protection, customer/admin separation' -ForegroundColor Green
    Write-Host "Created isolated smoke test user: $email"
  }
} finally {Pop-Location}
