$ErrorActionPreference = 'Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
if (-not (Get-Command javac -ErrorAction SilentlyContinue)) { throw 'Java 21 javac is not on PATH' }
$dest = Join-Path $env:TEMP ('MHAM_Phase8_Offline_' + [guid]::NewGuid().ToString('N'))
New-Item -Path $dest -ItemType Directory | Out-Null
try {
    & javac -encoding UTF-8 -d $dest 'src/main/java/com/mhamauto/admin/AdminRules.java' 'tests/Phase8OfflineCheck.java'
    if ($LASTEXITCODE -ne 0) { throw 'Phase 8 compilation failed' }
    & java -cp $dest Phase8OfflineCheck
    if ($LASTEXITCODE -ne 0) { throw 'Phase 8 validation failed' }
    Write-Host 'Phase 8 offline checks PASS' -ForegroundColor Green
} finally { Remove-Item -LiteralPath $dest -Recurse -Force -ErrorAction SilentlyContinue }
