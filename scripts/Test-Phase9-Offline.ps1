$ErrorActionPreference = 'Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
if (-not (Get-Command javac -ErrorAction SilentlyContinue)) { throw 'Java 21 javac required' }
$destination = Join-Path $env:TEMP ('MHAM_Phase9_Offline_' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $destination | Out-Null
try {
  & javac -encoding UTF-8 -d $destination `
    'src/main/java/com/mhamauto/checkout/AddressRules.java' `
    'src/main/java/com/mhamauto/trade/TradeRules.java' `
    'tests/Phase9OfflineCheck.java'
  if ($LASTEXITCODE -ne 0) { throw 'Phase 9 javac failed' }
  & java -cp $destination Phase9OfflineCheck
  if ($LASTEXITCODE -ne 0) { throw 'Phase 9 validation checks failed' }
  Write-Host 'PASS Phase 9 offline checks' -ForegroundColor Green
} finally {
  Remove-Item -LiteralPath $destination -Recurse -Force -ErrorAction SilentlyContinue
}
