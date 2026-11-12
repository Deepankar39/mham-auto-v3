param([switch]$SkipHistorical)
$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath (Split-Path $PSScriptRoot -Parent)
if (-not (Get-Command javac -ErrorAction SilentlyContinue)) { throw 'Java 21 javac is required' }
Write-Host 'MHAM AUTO RC1 - Phase 11 offline regression gate' -ForegroundColor Cyan
& java -version
& javac -version
if (-not $SkipHistorical) {
  $tests = @('Test-Phase1_3_Offline.ps1','Test-Phase1_4_Offline.ps1','Test-Phase2-Offline.ps1',
             'Test-Phase3-Offline.ps1','Test-Phase4-Offline.ps1','Test-Phase5-Offline.ps1',
             'Test-Phase6-Offline.ps1','Test-Phase7-Offline.ps1','Test-Phase8-Offline.ps1',
             'Test-Phase9-Offline.ps1','Test-Phase10-Offline.ps1')
  foreach ($test in $tests) {
    Write-Host "RUN $test" -ForegroundColor Cyan
    & powershell -NoProfile -ExecutionPolicy Bypass -File (Join-Path $PSScriptRoot $test)
    if ($LASTEXITCODE -ne 0) { throw "FAIL: $test" }
  }
}
if (Get-Command python -ErrorAction SilentlyContinue) {
   & python (Join-Path $PSScriptRoot 'phase11_static_audit.py')
   if ($LASTEXITCODE -ne 0) { throw 'Phase 11 static audit failed' }
} else {
   Write-Host 'Python is optional and unavailable: source static audit not run on this machine' -ForegroundColor Yellow
}
if (Get-Command node -ErrorAction SilentlyContinue) {
 foreach($j in @(Get-ChildItem 'src/main/webapp/assets' -Recurse -File -Filter '*.js')) {
     & node --check $j.FullName
     if ($LASTEXITCODE -ne 0) { throw "JavaScript syntax check failed for $($j.FullName)" }
 }
}
Write-Host 'PASS Phase 11 offline gate (all selected checks completed)' -ForegroundColor Green
