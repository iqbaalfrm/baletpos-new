$ErrorActionPreference = 'SilentlyContinue'
Write-Host "[CLEAN] Stop Java/Gradle processes..."
Get-Process java, javaw, gradle -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue

Start-Sleep -Milliseconds 500

Write-Host "[CLEAN] Remove build outputs..."
$paths = @(
  "build",
  "bin",
  ".gradle",
  "out",
  "target"
)
foreach ($p in $paths) {
  if (Test-Path $p) {
    Remove-Item $p -Recurse -Force -ErrorAction SilentlyContinue
  }
}

if (Test-Path ".\gradlew.bat") {
  Write-Host "[CLEAN] Running gradle clean via wrapper..."
  .\gradlew.bat clean
}

Write-Host "[CLEAN] Done."
