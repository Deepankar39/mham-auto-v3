param([switch]$SkipBuild,[switch]$SkipHttp)
$ErrorActionPreference='Stop'
$root=Split-Path -Parent $PSScriptRoot
Push-Location -LiteralPath $root
try {
  & powershell -NoProfile -ExecutionPolicy Bypass -File '.\scripts\Test-Phase5-Offline.ps1'
  if($LASTEXITCODE -ne 0){throw 'Phase 5 offline tests failed'}
  if(-not $SkipBuild) {
    & mvn clean package dependency:copy-dependencies '-DincludeScope=runtime' '-DoutputDirectory=target/dependency'
    if($LASTEXITCODE -ne 0){throw 'Maven build failed'}
  }
  if(-not $env:MHAM_DB_USER -or -not $env:MHAM_DB_PASSWORD){throw 'Set MHAM_DB_USER and MHAM_DB_PASSWORD in this PowerShell session'}
  $cp='target/classes;target/dependency/*'
  & java -cp $cp com.mhamauto.cli.CartCli status
  if($LASTEXITCODE -ne 0){throw 'V003 missing; run CartCli migrate first'}
  $variantOut=& java -cp $cp com.mhamauto.cli.CartCli sample-variant
  if($LASTEXITCODE -ne 0){throw 'No visible test variant available'}
  $variant=([string]($variantOut | Select-Object -Last 1)).Trim()
  if($variant -notmatch '^[1-9][0-9]*$'){throw "Unexpected variant ID: $variant"}
  Write-Host "Sample variant for cart tests: $variant"
  if(-not $SkipHttp){
    $base='http://localhost:8080/mham-auto'
    $s=New-Object Microsoft.PowerShell.Commands.WebRequestSession
    $page=Invoke-WebRequest -UseBasicParsing -Uri "$base/register" -WebSession $s -TimeoutSec 25
    $match=[regex]::Match($page.Content,'name="csrf"\s+value="([^"]+)"')
    if(-not $match.Success){throw 'Registration CSRF token missing'}
    $email='phase5-smoke-'+[guid]::NewGuid().ToString('N')+'@example.test'
    $form=@{csrf=$match.Groups[1].Value;name='Phase Five Test';email=$email;password='Safe-test-password-2026!';confirm='Safe-test-password-2026!'}
    $account=Invoke-WebRequest -UseBasicParsing -Method Post -Uri "$base/register" -Body $form -WebSession $s -TimeoutSec 25
    if($account.Content -notmatch 'Your customer account'){throw 'Test customer registration failed'}
    $cart=Invoke-WebRequest -UseBasicParsing -Uri "$base/cart" -WebSession $s -TimeoutSec 25
    if($cart.Content -notmatch 'Your cart is empty'){throw 'New cart not empty'}
    $token=[regex]::Match($cart.Content,'name="csrf"\s+value="([^"]+)"')
    if(-not $token.Success){throw 'Cart CSRF missing'}
    $t=$token.Groups[1].Value
    try {
      Invoke-WebRequest -UseBasicParsing -Method Post -Uri "$base/cart" -Body @{action='add';variantId=$variant;quantity='1';csrf='invalid'} -WebSession $s -TimeoutSec 25 | Out-Null
      throw 'Invalid CSRF was accepted'
    } catch {if(-not $_.Exception.Response -or [int]$_.Exception.Response.StatusCode -ne 403){throw}}
    try {
      Invoke-WebRequest -UseBasicParsing -Method Post -Uri "$base/cart" -Body @{action='add';variantId=$variant;quantity='21';csrf=$t} -WebSession $s -TimeoutSec 25 | Out-Null
      throw 'Invalid quantity was accepted'
    } catch {if(-not $_.Exception.Response -or [int]$_.Exception.Response.StatusCode -ne 400){throw}}
    $added=Invoke-WebRequest -UseBasicParsing -Method Post -Uri "$base/cart" -Body @{action='add';variantId=$variant;quantity='1';csrf=$t} -WebSession $s -TimeoutSec 25
    if($added.Content -notmatch 'Item added to your cart'){throw 'Adding item failed'}
    $updated=Invoke-WebRequest -UseBasicParsing -Method Post -Uri "$base/cart" -Body @{action='update';variantId=$variant;quantity='2';csrf=$t} -WebSession $s -TimeoutSec 25
    if($updated.Content -notmatch 'Quantity updated'){throw 'Updating quantity failed'}
    $badge=Invoke-WebRequest -UseBasicParsing -Uri "$base/api/cart/count" -WebSession $s -TimeoutSec 25
    if($badge.Content -notmatch '"count":2'){throw 'Cart badge was not updated to 2'}
    $second=New-Object Microsoft.PowerShell.Commands.WebRequestSession
    $guest=Invoke-WebRequest -UseBasicParsing -Uri "$base/api/cart/count" -WebSession $second -TimeoutSec 25
    if($guest.Content -notmatch '"count":0'){throw 'Guest count is not isolated'}
    $guestCart=Invoke-WebRequest -UseBasicParsing -Uri "$base/cart" -WebSession $second -TimeoutSec 25
    if($guestCart.Content -notmatch 'Welcome back'){throw 'Anonymous cart access was not redirected to login'}
    # Log out and log back in: cart must persist in database, not only an in-memory session.
    $accountPage=Invoke-WebRequest -UseBasicParsing -Uri "$base/account" -WebSession $s -TimeoutSec 25
    $logoutToken=[regex]::Match($accountPage.Content,'name="csrf"\s+value="([^"]+)"')
    if(-not $logoutToken.Success){throw 'Logout CSRF missing'}
    $out=Invoke-WebRequest -UseBasicParsing -Method Post -Uri "$base/logout" -Body @{csrf=$logoutToken.Groups[1].Value} -WebSession $s -TimeoutSec 25
    if($out.Content -notmatch 'Welcome back'){throw 'Logout failed'}
    $signin=Invoke-WebRequest -UseBasicParsing -Uri "$base/login" -WebSession $s -TimeoutSec 25
    $signinToken=[regex]::Match($signin.Content,'name="csrf"\s+value="([^"]+)"')
    if(-not $signinToken.Success){throw 'Login CSRF missing'}
    $afterLogin=Invoke-WebRequest -UseBasicParsing -Method Post -Uri "$base/login" -Body @{email=$email;password='Safe-test-password-2026!';csrf=$signinToken.Groups[1].Value} -WebSession $s -TimeoutSec 25
    if($afterLogin.Content -notmatch 'Your customer account'){throw 'Login failed'}
    $persist=Invoke-WebRequest -UseBasicParsing -Uri "$base/api/cart/count" -WebSession $s -TimeoutSec 25
    if($persist.Content -notmatch '"count":2'){throw 'Cart did not persist across logout and login'}
    # Account B gets an empty cart and cannot see account A items.
    $regB=Invoke-WebRequest -UseBasicParsing -Uri "$base/register" -WebSession $second -TimeoutSec 25
    $csrfB=[regex]::Match($regB.Content,'name="csrf"\s+value="([^"]+)"')
    if(-not $csrfB.Success){throw 'Second account registration CSRF missing'}
    $emailB='phase5-other-'+[guid]::NewGuid().ToString('N')+'@example.test'
    $other=Invoke-WebRequest -UseBasicParsing -Method Post -Uri "$base/register" -Body @{csrf=$csrfB.Groups[1].Value;name='Second Test Customer';email=$emailB;password='Safe-test-password-2026!';confirm='Safe-test-password-2026!'} -WebSession $second -TimeoutSec 25
    if($other.Content -notmatch 'Your customer account'){throw 'Second account failed'}
    $otherCart=Invoke-WebRequest -UseBasicParsing -Uri "$base/cart" -WebSession $second -TimeoutSec 25
    if($otherCart.Content -notmatch 'Your cart is empty'){throw 'Cross-account cart data leak'}
    $afterReauthCart=Invoke-WebRequest -UseBasicParsing -Uri "$base/cart" -WebSession $s -TimeoutSec 25
    $freshCartToken=[regex]::Match($afterReauthCart.Content,'name="csrf"\s+value="([^"]+)"')
    if(-not $freshCartToken.Success){throw 'Fresh cart CSRF after login is missing'}
    $removed=Invoke-WebRequest -UseBasicParsing -Method Post -Uri "$base/cart" -Body @{action='remove';variantId=$variant;csrf=$freshCartToken.Groups[1].Value} -WebSession $s -TimeoutSec 25
    if($removed.Content -notmatch 'Your cart is empty'){throw 'Removing cart line failed'}
    Write-Host 'PASS Phase 5 HTTP: accounts, CSRF, validation, add, update, count, persistence, account isolation, remove' -ForegroundColor Green
    Write-Host "Isolated test account: $email"
  }
  Write-Host 'PASS Phase 5 validation complete' -ForegroundColor Green
} finally {Pop-Location}
