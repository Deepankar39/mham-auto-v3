param([switch]$SkipBuild,[switch]$SkipHttp)
$ErrorActionPreference='Stop'
$root=Split-Path -Parent $PSScriptRoot
Push-Location -LiteralPath $root
try {
  & powershell -NoProfile -ExecutionPolicy Bypass -File '.\scripts\Test-Phase6-Offline.ps1'
  if($LASTEXITCODE -ne 0){throw 'Offline check failed'}
  if(-not $SkipBuild){
    & mvn clean package dependency:copy-dependencies '-DincludeScope=runtime' '-DoutputDirectory=target/dependency'
    if($LASTEXITCODE -ne 0){throw 'Maven build failed'}
  }
  if(-not $env:MHAM_DB_USER -or -not $env:MHAM_DB_PASSWORD){throw 'Set MHAM_DB_USER and MHAM_DB_PASSWORD first'}
  $cp='target/classes;target/dependency/*'
  & java -cp $cp com.mhamauto.cli.CheckoutCli status
  if($LASTEXITCODE -ne 0){throw 'V004 not installed'}
  $variantOut = & java -cp $cp com.mhamauto.cli.CheckoutCli sample-priced
  if($LASTEXITCODE -ne 0){throw 'No KWD-priced demo product available for checkout test'}
  $variant=([string]($variantOut | Select-Object -Last 1)).Trim()
  if($variant -notmatch '^[1-9][0-9]*$'){throw "Invalid sample variant ID: $variant"}
  if(-not $SkipHttp){
    $base='http://localhost:8080/mham-auto'
    $client=New-Object Microsoft.PowerShell.Commands.WebRequestSession
    $register=Invoke-WebRequest -UseBasicParsing -WebSession $client -Uri "$base/register" -TimeoutSec 25
    $csrf=[regex]::Match($register.Content,'name="csrf"\s+value="([^"]+)"')
    if(-not $csrf.Success){throw 'Registration CSRF missing'}
    $email='phase6-'+[guid]::NewGuid().ToString('N')+'@example.test'
    $registered=Invoke-WebRequest -UseBasicParsing -Method Post -Uri "$base/register" -WebSession $client -Body @{csrf=$csrf.Groups[1].Value;name='Demo Phase Six';email=$email;password='Safe-test-password-2026!';confirm='Safe-test-password-2026!'} -TimeoutSec 25
    if($registered.Content -notmatch 'Your customer account'){throw 'Registration did not create account'}
    $addrPage=Invoke-WebRequest -UseBasicParsing -Uri "$base/addresses" -WebSession $client -TimeoutSec 25
    $t=[regex]::Match($addrPage.Content,'name="csrf"\s+value="([^"]+)"')
    if(-not $t.Success){throw 'Address CSRF missing'}
    $invalidCsrf=@{action='save';csrf='invalid';recipientName='Test User';phone='51234567';governorate='HAWALLI';area='Salmiya';block='4';street='Test Road';building='22'}
    try {Invoke-WebRequest -UseBasicParsing -Method Post -Uri "$base/addresses" -WebSession $client -Body $invalidCsrf -TimeoutSec 25 | Out-Null;throw 'Invalid CSRF accepted'}
    catch {if(-not $_.Exception.Response -or [int]$_.Exception.Response.StatusCode -ne 403){throw}}
    $addressForm=@{action='save';csrf=$t.Groups[1].Value;recipientName='Demo Phase Six';phone='51234567';governorate='HAWALLI';area='Salmiya';block='4';street='Test Road';building='22';floorDetail='3';directions='Academic example'}
    $saved=Invoke-WebRequest -UseBasicParsing -Method Post -Uri "$base/addresses" -WebSession $client -Body $addressForm -TimeoutSec 25
    if($saved.Content -notmatch 'Address saved'){throw 'Address save failed'}
    $addressMatch=[regex]::Match($saved.Content,'name="addressId"\s+value="([0-9]+)"')
    if(-not $addressMatch.Success){throw 'Saved address ID not shown'}
    $address=$addressMatch.Groups[1].Value
    $cartPage=Invoke-WebRequest -UseBasicParsing -Uri "$base/cart" -WebSession $client -TimeoutSec 25
    $cartToken=[regex]::Match($cartPage.Content,'name="csrf"\s+value="([^"]+)"')
    if(-not $cartToken.Success){throw 'Cart CSRF missing'}
    $added=Invoke-WebRequest -UseBasicParsing -Method Post -Uri "$base/cart" -WebSession $client -Body @{csrf=$cartToken.Groups[1].Value;action='add';variantId=$variant;quantity='2'} -TimeoutSec 25
    if($added.Content -notmatch 'Item added'){throw 'Cart add failed'}
    $checkout=Invoke-WebRequest -UseBasicParsing -Uri "$base/checkout" -WebSession $client -TimeoutSec 25
    if($checkout.Content -notmatch 'Place DEMO order'){throw 'Checkout action is missing or blocked'}
    $cToken=[regex]::Match($checkout.Content,'name="checkoutToken"\s+value="([^"]+)"')
    $csrfCheck=[regex]::Match($checkout.Content,'name="csrf"\s+value="([^"]+)"')
    if(-not $cToken.Success -or -not $csrfCheck.Success){throw 'Checkout tokens missing'}
    try{Invoke-WebRequest -UseBasicParsing -Method Post -Uri "$base/checkout" -WebSession $client -Body @{csrf='invalid';checkoutToken=$cToken.Groups[1].Value;addressId=$address} -TimeoutSec 25|Out-Null;throw 'Bad checkout CSRF accepted'}
    catch{if(-not $_.Exception.Response -or [int]$_.Exception.Response.StatusCode -ne 403){throw}}
    $form=@{csrf=$csrfCheck.Groups[1].Value;checkoutToken=$cToken.Groups[1].Value;addressId=$address}
    $placed=Invoke-WebRequest -UseBasicParsing -Method Post -Uri "$base/checkout" -WebSession $client -Body $form -TimeoutSec 25
    $refMatch=[regex]::Match($placed.Content,'DEMO-[A-F0-9]{20}')
    if(-not $refMatch.Success){throw 'No demo order reference shown'}
    $ref=$refMatch.Value
    $replayed=Invoke-WebRequest -UseBasicParsing -Method Post -Uri "$base/checkout" -WebSession $client -Body $form -TimeoutSec 25
    if($replayed.Content -notmatch $ref){throw 'Duplicate submit created a different order'}
    $cartAfter=Invoke-WebRequest -UseBasicParsing -Uri "$base/cart" -WebSession $client -TimeoutSec 25
    if($cartAfter.Content -notmatch 'Your cart is empty'){throw 'Cart was not cleared after checkout'}
    $different=New-Object Microsoft.PowerShell.Commands.WebRequestSession
    $guest=Invoke-WebRequest -UseBasicParsing -Uri "$base/order-confirmation?ref=$ref" -WebSession $different -TimeoutSec 25
    if($guest.Content -notmatch 'Welcome back'){throw 'Guest order confirmation was accessible'}
    $otherPage=Invoke-WebRequest -UseBasicParsing -Uri "$base/register" -WebSession $different -TimeoutSec 25
    $otherToken=[regex]::Match($otherPage.Content,'name="csrf"\s+value="([^"]+)"')
    $other=Invoke-WebRequest -UseBasicParsing -Method Post -Uri "$base/register" -WebSession $different -Body @{csrf=$otherToken.Groups[1].Value;name='Other Test User';email=('phase6-other-'+[guid]::NewGuid().ToString('N')+'@example.test');password='Safe-test-password-2026!';confirm='Safe-test-password-2026!'} -TimeoutSec 25
    try{Invoke-WebRequest -UseBasicParsing -Uri "$base/order-confirmation?ref=$ref" -WebSession $different -TimeoutSec 25|Out-Null;throw 'Other customer viewed order'}
    catch{if(-not $_.Exception.Response -or [int]$_.Exception.Response.StatusCode -ne 404){throw}}
    Write-Host 'PASS HTTP: registration, address, CSRF, cart, checkout, idempotency, cart cleanup, ownership' -ForegroundColor Green
    Write-Host "Test customer: $email  Demo order: $ref"
  }
  Write-Host 'PASS Phase 6 verification' -ForegroundColor Green
}finally{Pop-Location}
