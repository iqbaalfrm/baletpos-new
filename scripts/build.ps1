$ErrorActionPreference = 'Stop'
Write-Host "[BUILD] Running clean first..."
& "$PSScriptRoot\clean.ps1"

Write-Host "[BUILD] Building project..."
if (Test-Path ".\gradlew.bat") {
  .\gradlew.bat clean build
} elseif (Get-Command gradle -ErrorAction SilentlyContinue) {
  gradle clean build
} else {
  Write-Host "Gradle not found in PATH. Please install Gradle or add to PATH." -ForegroundColor Red
  exit 1
}
