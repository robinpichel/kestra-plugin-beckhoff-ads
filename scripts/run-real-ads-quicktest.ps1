param(
    [string]$TargetIp = "localhost",
    [string]$TargetAmsNetId = "199.4.42.250.1.1",
    [string]$TargetAmsPort = "851",
    [string]$Variable = "MAIN.nAdsKestraTest",
    [string]$DataType = "DINT",
    [string]$LocalAmsNetId = "",
    [string]$AutoAddRoute = "false"
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

$jreCandidates = Get-ChildItem -Path "$env:USERPROFILE/.vscode/extensions" -Recurse -Filter java.exe -ErrorAction SilentlyContinue |
    Sort-Object FullName -Descending

if (-not $jreCandidates -or $jreCandidates.Count -eq 0) {
    throw "No java.exe found under VS Code Java extension. Install the Red Hat Java extension or set JAVA_HOME manually."
}

$javaExe = $jreCandidates[0].FullName
$jreHome = Split-Path -Parent (Split-Path -Parent $javaExe)

$nativeDir = Join-Path $repoRoot "libs/native-win"
$dllPath = Join-Path $nativeDir "AdsToJava-3.dll"
$zipPath = Join-Path $nativeDir "win-x64.zip"

if (-not (Test-Path $dllPath)) {
    New-Item -ItemType Directory -Path $nativeDir -Force | Out-Null
    Invoke-WebRequest -Uri "https://github.com/Beckhoff/AdsToJava/releases/download/3.1.0-32/win-x64.zip" -OutFile $zipPath
    Expand-Archive -Path $zipPath -DestinationPath $nativeDir -Force
}

if (-not (Test-Path $dllPath)) {
    throw "AdsToJava-3.dll not found after extraction at $dllPath"
}

$env:JAVA_HOME = $jreHome
$env:PATH = "$jreHome/bin;$nativeDir;" + $env:PATH
$env:JAVA_TOOL_OPTIONS = "-Djava.library.path=$nativeDir"
$env:ADS_REAL_SMOKE_TEST = "true"
$env:ADS_TARGET_IP = $TargetIp
$env:ADS_TARGET_AMS_NET_ID = $TargetAmsNetId
$env:ADS_TARGET_AMS_PORT = $TargetAmsPort
$env:ADS_REAL_VARIABLE = $Variable
$env:ADS_REAL_DATATYPE = $DataType
$env:ADS_LOCAL_AMS_NET_ID = $LocalAmsNetId
$env:ADS_AUTO_ADD_ROUTE = $AutoAddRoute

Write-Host "Running real ADS quick test with variable: $Variable"
Write-Host "Target: $TargetIp | AMS Net ID: $TargetAmsNetId | Port: $TargetAmsPort"

./gradlew.bat realAdsQuickTest --no-daemon -x jacocoTestReport
