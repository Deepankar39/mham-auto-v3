param([string]$BaseUrl='http://localhost:8080/mham-auto',[int]$Runs=5)
$ErrorActionPreference='Stop'
if($Runs -lt 1 -or $Runs -gt 100){throw 'Runs must be between 1 and 100'}
foreach($path in @('/','/catalog','/products','/health')) {
  $times = @()
  for($i=0;$i -lt $Runs;$i++) {
    $sw=[System.Diagnostics.Stopwatch]::StartNew()
    $res=Invoke-WebRequest -Uri ($BaseUrl.TrimEnd('/')+$path) -UseBasicParsing -TimeoutSec 25
    $sw.Stop()
    if($res.StatusCode -ne 200){throw "Unexpected status for $path"}
    $times += $sw.Elapsed.TotalMilliseconds
  }
  $avg=[math]::Round((($times|Measure-Object -Average).Average),1)
  $max=[math]::Round((($times|Measure-Object -Maximum).Maximum),1)
  Write-Host "$path (runs=$Runs): mean ${avg}ms, max ${max}ms"
}
Write-Host 'These are local warm/cold mixed observations, NOT 1M+ product benchmarks.'
