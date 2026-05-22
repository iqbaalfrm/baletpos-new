param(
    [Parameter(Mandatory = $true)]
    [string]$DbPassword,

    [string]$ProjectRef = "wlubhewrckakwghmcsxi",
    [string]$PoolerHost = "aws-1-ap-northeast-2.pooler.supabase.com",
    [string]$PoolerPort = "6543"
)

$configDir = Join-Path $env:USERPROFILE ".baletpos"
$configPath = Join-Path $configDir "config.properties"

New-Item -ItemType Directory -Force -Path $configDir | Out-Null

$escapedPassword = $DbPassword.Replace("\", "\\").Replace(":", "\:").Replace("=", "\=")
$content = @"
# BaletPOS local settings
baletpos.db.url=jdbc\:postgresql\://$PoolerHost\:$PoolerPort/postgres?sslmode\=require&prepareThreshold\=0
baletpos.db.user=postgres.$ProjectRef
baletpos.db.password=$escapedPassword
"@

Set-Content -LiteralPath $configPath -Value $content -Encoding ASCII
Write-Host "Desktop config written to $configPath"
