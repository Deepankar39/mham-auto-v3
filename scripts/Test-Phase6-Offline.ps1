$ErrorActionPreference='Stop'
$root=Split-Path -Parent $PSScriptRoot
Push-Location -LiteralPath $root
try {
  $dir='target/phase6-offline'
  New-Item -ItemType Directory -Force -Path $dir | Out-Null
  & javac -encoding UTF-8 -d $dir '.\src\main\java\com\mhamauto\checkout\AddressRules.java' '.\src\main\java\com\mhamauto\checkout\CheckoutRules.java' '.\tests\phase-6\Phase6CheckoutCheck.java'
  if($LASTEXITCODE -ne 0){throw 'Phase 6 javac failed'}
  & java -cp $dir com.mhamauto.checkout.Phase6CheckoutCheck
  if($LASTEXITCODE -ne 0){throw 'Phase 6 rule tests failed'}
} finally { Pop-Location }
