param(
    [int] $Port = 8087,
    [string] $BindAddress = "0.0.0.0",
    [switch] $NoOpen
)

$ErrorActionPreference = "Stop"

$scriptDirectory = Split-Path -Parent $MyInvocation.MyCommand.Path
$repoDirectory = Split-Path -Parent $scriptDirectory

$bundledJdk = Join-Path $repoDirectory ".gradle\ebsl-jdks\microsoft-jdk-21-x64"
if (-not $env:JAVA_HOME -and (Test-Path (Join-Path $bundledJdk "bin\java.exe"))) {
    $env:JAVA_HOME = $bundledJdk
}

if ($env:JAVA_HOME) {
    $env:Path = "$(Join-Path $env:JAVA_HOME "bin");$env:Path"
}

$bundledNode = Join-Path $env:USERPROFILE ".cache\codex-runtimes\codex-primary-runtime\dependencies\node\bin\node.exe"
if (-not $env:NODE_EXECUTABLE -and (Test-Path $bundledNode)) {
    $env:NODE_EXECUTABLE = $bundledNode
}

Set-Location $repoDirectory
& .\gradlew.bat :tools:pathfinder-sim-viewer:syncViewerApp
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

$viewerDirectory = Join-Path $repoDirectory "build\tools\pathfinder-sim-viewer\webapp"
$javaExecutable = Join-Path $env:JAVA_HOME "bin\java.exe"
$url = "http://localhost:$Port"

Write-Host "Serving pathfinder sim viewer at $url"
Write-Host "Bound to $BindAddress for LAN/mobile testing."
Write-Host "Press Ctrl+C to stop the viewer server."

if (-not $NoOpen) {
    Start-Job -ScriptBlock {
        param($ViewerUrl)
        Start-Sleep -Seconds 2
        Start-Process $ViewerUrl
    } -ArgumentList $url | Out-Null
}

& $javaExecutable -m jdk.httpserver -b $BindAddress -p $Port -d $viewerDirectory
