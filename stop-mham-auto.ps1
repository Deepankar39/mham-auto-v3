$ErrorActionPreference = "Stop"

$tomcat = "D:\Java Tools\apache-tomcat-10.1.61"

function Get-TomcatProcess {
    Get-CimInstance Win32_Process -Filter "Name='java.exe'" |
    Where-Object {
        $_.CommandLine -and
        $_.CommandLine.Contains($tomcat) -and
        $_.CommandLine -match 'org\.apache\.catalina\.startup\.Bootstrap\s+start'
    }
}

Write-Host "`n================================="
Write-Host "        MHAM AUTO V3 STOP"
Write-Host "================================="

$processes = @(Get-TomcatProcess)

if ($processes.Count -eq 0) {
    Write-Host "Tomcat is already stopped."
}
else {
    $env:CATALINA_HOME = $tomcat
    $env:CATALINA_BASE = $tomcat

    Write-Host "Tomcat process found: $($processes.ProcessId)"
    Write-Host "Requesting graceful shutdown..."

    & "$tomcat\bin\shutdown.bat"

    $stopped = $false

    for ($i = 1; $i -le 30; $i++) {
        Start-Sleep -Seconds 2
        $processes = @(Get-TomcatProcess)

        if ($processes.Count -eq 0) {
            $stopped = $true
            break
        }
    }

    if (-not $stopped) {
        Write-Warning "Tomcat has not exited after 60 seconds."

        $answer = Read-Host "Force-stop the identified Tomcat process(es)? (Y/N)"

        if ($answer -eq "Y") {
            foreach ($process in $processes) {
                Stop-Process -Id $process.ProcessId -Force
            }

            Start-Sleep -Seconds 3
            $processes = @(Get-TomcatProcess)
            $stopped = ($processes.Count -eq 0)
        }
    }

    if ($stopped) {
        Write-Host "Tomcat process stopped successfully."
    }
    else {
        Write-Warning "Tomcat is still running. No further process was terminated."
    }
}

$remaining = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue

if ($remaining) {
    Write-Warning "Port 8080 is still listening."
}
else {
    Write-Host "Port 8080 is free."
}

if (@(Get-TomcatProcess).Count -eq 0) {
    Remove-Item Env:MHAM_DB_PASSWORD -ErrorAction SilentlyContinue
    Remove-Item Env:MHAM_DB_USER -ErrorAction SilentlyContinue
    Write-Host "Database credentials cleared from this PowerShell session."
}

