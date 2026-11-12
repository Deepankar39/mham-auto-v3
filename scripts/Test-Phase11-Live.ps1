param([string]$BaseUrl='http://localhost:8080/mham-auto')
$ErrorActionPreference='Stop'
Add-Type -AssemblyName System.Net.Http
$base=$BaseUrl.TrimEnd('/')
$handler=New-Object System.Net.Http.HttpClientHandler
$handler.AllowAutoRedirect=$false
$client=[System.Net.Http.HttpClient]::new($handler)
$client.Timeout=[TimeSpan]::FromSeconds(20)
try {
 function Check([string]$path,[int[]]$expected) {
   $r=$client.GetAsync($base+$path).GetAwaiter().GetResult()
   $code=[int]$r.StatusCode
   if ($expected -notcontains $code) { throw "FAIL GET $path - got $code, expected $($expected -join ',')" }
   Write-Host "PASS GET $path => $code"
   return $r
 }
$homePage=Check '/' @(200)
if (-not $homePage.Headers.Contains('X-Content-Type-Options')) { throw 'Missing nosniff header' }
if (-not $homePage.Headers.Contains('Content-Security-Policy')) { throw 'Missing CSP header' }
 foreach($path in @('/catalog','/products','/health','/api/db-status','/login','/register','/assets/css/phase10.css')) {
   [void](Check $path @(200))
 }
 foreach($path in @('/account','/orders','/cart','/addresses','/checkout','/trade/apply',
                  '/trade/status','/admin','/admin/products','/admin/trade-applications')) {
   [void](Check $path @(302,303))
 }
 $req=[System.Net.Http.HttpRequestMessage]::new([System.Net.Http.HttpMethod]::Post,$base+'/login')
 $req.Headers.TryAddWithoutValidation('Origin','http://untrusted.example') | Out-Null
 $req.Content=[System.Net.Http.StringContent]::new('email=bad%40example.com')
 $r=$client.SendAsync($req).GetAwaiter().GetResult()
 if ([int]$r.StatusCode -ne 403) { throw "Expected cross-origin POST 403, got $([int]$r.StatusCode)" }
 Write-Host 'PASS cross-origin POST rejected with 403' -ForegroundColor Green
 Write-Host 'PASS public HTTP, protected anonymous routes and basic headers. Authenticated user flows still require manual UAT.' -ForegroundColor Green
} finally { $client.Dispose(); $handler.Dispose() }
