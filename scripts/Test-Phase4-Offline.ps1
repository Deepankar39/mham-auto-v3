$ErrorActionPreference='Stop'
$root=Split-Path -Parent $PSScriptRoot
Push-Location -LiteralPath $root
try {
    & powershell -NoProfile -ExecutionPolicy Bypass -File '.\scripts\Test-Phase3-Offline.ps1'
    if ($LASTEXITCODE -ne 0) {throw 'Phase 3 regression failed'}
    $tmp=Join-Path $env:TEMP ('mham-phase4-'+[guid]::NewGuid().ToString('N'))
    New-Item -ItemType Directory -Force -Path $tmp | Out-Null
    try {
        javac -encoding UTF-8 -d $tmp `
            '.\src\main\java\com\mhamauto\auth\IdentityRules.java' `
            '.\src\main\java\com\mhamauto\auth\PasswordHasher.java' `
            '.\src\main\java\com\mhamauto\auth\AuthPrincipal.java' `
            '.\src\main\java\com\mhamauto\auth\LoginThrottle.java' `
            '.\tests\phase-4\Phase4AuthCheck.java'
        if ($LASTEXITCODE -ne 0) {throw 'Phase 4 Java compilation failed'}
        java -cp $tmp Phase4AuthCheck
        if ($LASTEXITCODE -ne 0) {throw 'Phase 4 plain Java checks failed'}
    } finally {Remove-Item $tmp -Recurse -Force -ErrorAction SilentlyContinue}
    Write-Host 'PASS: Phase 4 offline checks and Phase 3 regression' -ForegroundColor Green
} finally {Pop-Location}
