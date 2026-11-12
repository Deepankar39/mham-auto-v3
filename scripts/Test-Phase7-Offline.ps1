$ErrorActionPreference='Stop'
$root=Split-Path -Parent $PSScriptRoot
Push-Location -LiteralPath $root
try {
  $dir='target/phase7-offline'
  New-Item -ItemType Directory -Force -Path $dir | Out-Null
  & javac -encoding UTF-8 -d $dir '.\src\main\java\com\mhamauto\auth\IdentityRules.java' '.\src\main\java\com\mhamauto\account\PortalRules.java' '.\tests\phase-7\Phase7PortalCheck.java'
  if($LASTEXITCODE -ne 0){throw 'Phase 7 javac failed'}
  & java -cp $dir com.mhamauto.account.Phase7PortalCheck
  if($LASTEXITCODE -ne 0){throw 'Phase 7 rule tests failed'}
} finally { Pop-Location }
