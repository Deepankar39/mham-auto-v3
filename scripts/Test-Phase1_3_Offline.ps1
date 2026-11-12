# Compile and test plain Java business/service classes without JUnit or a database.
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$build = Join-Path $root 'target\phase13-test-classes'
New-Item -ItemType Directory -Path $build -Force | Out-Null
$files = @(
  (Join-Path $root 'src\main\java\com\mhamauto\model\CatalogPreviewCategory.java'),
  (Join-Path $root 'src\main\java\com\mhamauto\service\CatalogPreviewService.java'),
  (Join-Path $root 'tests\phase-1.3\CatalogPreviewServiceCheck.java')
)
& javac -encoding UTF-8 -d $build @files
if ($LASTEXITCODE -ne 0) { throw 'Java compilation failed' }
& java -cp $build CatalogPreviewServiceCheck
if ($LASTEXITCODE -ne 0) { throw 'Offline Java smoke test failed' }
