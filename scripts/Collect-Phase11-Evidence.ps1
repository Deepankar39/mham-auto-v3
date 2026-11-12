param([switch]$IncludeDatabase,[switch]$IncludeHttp,[string]$BaseUrl='http://localhost:8080/mham-auto')
$ErrorActionPreference = 'Stop'
$root=Split-Path $PSScriptRoot -Parent
Set-Location -LiteralPath $root
$results='evidence/results'
New-Item -ItemType Directory -Path $results -Force | Out-Null
function Record([string]$label,[string]$script,[object[]]$params=@()) {
 $log=Join-Path $results ($label+'.txt')
 & powershell -NoProfile -ExecutionPolicy Bypass -File $script @params 2>&1 | Out-File -FilePath $log -Encoding utf8
 $status=$LASTEXITCODE
 Get-Content -LiteralPath $log | Select-Object -Last 12
 if ($status -ne 0) { throw "$label failed - see $log" }
 Write-Host "Saved $log" -ForegroundColor Green
}
Record 'phase11-offline' '.\scripts\Test-Phase11-Offline.ps1'
$mvnlog=Join-Path $results 'phase11-maven.txt'
& mvn clean test package dependency:copy-dependencies '-DincludeScope=runtime' '-DoutputDirectory=target/dependency' 2>&1 | Out-File -FilePath $mvnlog -Encoding utf8
if ($LASTEXITCODE -ne 0) { throw "Maven failed - see $mvnlog" }
if ($IncludeDatabase) { Record 'phase11-database' '.\scripts\Test-Phase11-Database.ps1' }
if ($IncludeHttp) { Record 'phase11-http' '.\scripts\Test-Phase11-Live.ps1' @('-BaseUrl',$BaseUrl) }
Write-Host 'Evidence saved. Complete manual UAT and screenshot checklist before signing off.' -ForegroundColor Green
