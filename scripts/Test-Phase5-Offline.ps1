$ErrorActionPreference='Stop'
$root=Split-Path -Parent $PSScriptRoot
Push-Location -LiteralPath $root
try {
  $dir='target/phase5-offline'
  New-Item -Path $dir -ItemType Directory -Force | Out-Null
  & javac -encoding UTF-8 -d $dir '.\src\main\java\com\mhamauto\media\ExternalImageUrlPolicy.java' '.\src\main\java\com\mhamauto\cart\CartRules.java' '.\src\main\java\com\mhamauto\cart\CartModels.java' '.\tests\phase-5\Phase5CartCheck.java'
  if($LASTEXITCODE -ne 0){throw 'Phase 5 javac failed'}
  & java -cp $dir com.mhamauto.cart.Phase5CartCheck
  if($LASTEXITCODE -ne 0){throw 'Phase 5 offline tests failed'}
} finally {Pop-Location}
