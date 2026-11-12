$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
& powershell -NoProfile -ExecutionPolicy Bypass -File (Join-Path $root 'scripts\Test-Phase1_4_Offline.ps1')
if ($LASTEXITCODE -ne 0) { throw 'Phase 1.4 regression failed' }
$target = Join-Path $root 'target\phase2-plain-check'
New-Item -ItemType Directory -Force -Path $target | Out-Null
$files = @(
 'src\main\java\com\mhamauto\media\ExternalImageUrlPolicy.java',
 'src\main\java\com\mhamauto\db\DbConfig.java',
 'src\main\java\com\mhamauto\importer\ShopifyRecord.java',
 'tests\phase-2\Phase2PlainJavaCheck.java'
) | ForEach-Object { Join-Path $root $_ }
& javac -encoding UTF-8 -d $target @files
if ($LASTEXITCODE -ne 0) { throw 'Phase 2 plain Java compilation failed' }
& java -cp $target Phase2PlainJavaCheck
if ($LASTEXITCODE -ne 0) { throw 'Phase 2 plain Java regression failed' }
Write-Host 'PASS Phase 2 offline smoke' -ForegroundColor Green
