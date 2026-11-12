$ErrorActionPreference='Stop'
$root=Split-Path -Parent $PSScriptRoot
Push-Location -LiteralPath $root
try {
    $output=Join-Path $env:TEMP ('mham-phase3-offline-' + [guid]::NewGuid().ToString('N'))
    New-Item -Path $output -ItemType Directory | Out-Null
    try {
        & javac --release 21 -encoding UTF-8 -d $output `
            'src/main/java/com/mhamauto/media/ExternalImageUrlPolicy.java' `
            'src/main/java/com/mhamauto/catalog/CatalogQuery.java' `
            'src/main/java/com/mhamauto/catalog/CatalogModels.java' `
            'src/main/java/com/mhamauto/catalog/DescriptionText.java' `
            'src/main/java/com/mhamauto/catalog/CatalogDao.java' `
            'tests/phase-3/Phase3QueryCheck.java'
        if ($LASTEXITCODE -ne 0) {throw 'Java 21 offline compilation failed'}
        & java -cp $output Phase3QueryCheck
        if ($LASTEXITCODE -ne 0) {throw 'Phase 3 Java checks failed'}
        foreach($file in @('src/main/webapp/WEB-INF/views/products.jsp','src/main/webapp/WEB-INF/views/product-detail.jsp','src/main/webapp/assets/css/products.css','src/main/webapp/assets/js/products.js')) {
            if (-not (Test-Path -LiteralPath $file)) {throw "Missing Phase 3 asset $file"}
        }
        Write-Host 'PASS Phase 3 offline checks' -ForegroundColor Green
    } finally {Remove-Item -LiteralPath $output -Recurse -Force -ErrorAction SilentlyContinue}
} finally {Pop-Location}
