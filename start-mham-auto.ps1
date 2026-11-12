$ErrorActionPreference = "Stop"

$tomcat = "D:\Java Tools\apache-tomcat-10.1.61"
$serviceName = "MYSQL84"
$healthUrl = "http://localhost:8080/mham-auto/health"

Write-Host "`n================================="
Write-Host "       MHAM AUTO V3 START"
Write-Host "================================="

# Check MySQL
$mysql = Get-Service -Name $serviceName

if ($mysql.Status -ne "Running") {
    Write-Host "Starting MySQL..."
    Start-Service -Name $serviceName
    $mysql.WaitForStatus("Running", [TimeSpan]::FromSeconds(30))
}

Write-Host "MySQL is running."

# Check if Tomcat is already running
$listener = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue

if ($listener) {
    try {
        $r = Invoke-WebRequest $healthUrl -UseBasicParsing -TimeoutSec 5
        Write-Host "MHAM AUTO is already running."
        Write-Host "URL: http://localhost:8080/mham-auto/"
        Start-Process "http://localhost:8080/mham-auto/"
        exit
    }
    catch {
        throw "Port 8080 is occupied but MHAM AUTO is not responding. Investigate before starting another Tomcat."
    }
}

# Configure Tomcat
$env:CATALINA_HOME = $tomcat
$env:CATALINA_BASE = $tomcat
$env:MHAM_DB_USER = "mham_app"

# Securely request database password
$securePassword = Read-Host "Enter MySQL password for mham_app" -AsSecureString
$bstr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)

try {
    $env:MHAM_DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($bstr)
}
finally {
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($bstr)
}

# Start Tomcat
Write-Host "`nStarting Tomcat..."
& "$tomcat\bin\startup.bat"

# Wait for application health check
$ready = $false

for ($i = 1; $i -le 30; $i++) {
    Start-Sleep -Seconds 2

    try {
        $response = Invoke-WebRequest $healthUrl -UseBasicParsing -TimeoutSec 4

        if ([int]$response.StatusCode -eq 200) {
            $ready = $true
            break
        }
    }
    catch {}
}

if (-not $ready) {
    Write-Host "`nApplication did not become ready."
    Write-Host "Check the latest Tomcat logs:"
    Get-ChildItem "$tomcat\logs\catalina.*.log" |
        Sort-Object LastWriteTime -Descending |
        Select-Object -First 1 |
        ForEach-Object { Get-Content $_.FullName -Tail 50 }

    throw "MHAM AUTO startup verification failed."
}

Write-Host "`n================================="
Write-Host " MHAM AUTO V3 IS RUNNING"
Write-Host "================================="
Write-Host "URL: http://localhost:8080/mham-auto/"

Start-Process "http://localhost:8080/mham-auto/"
