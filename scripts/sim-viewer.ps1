param(
    [int] $Port = 8087,
    [string] $BindAddress = "0.0.0.0",
    [string] $ReplayDir = "",
    [string] $WorldDir = "run\saves",
    [switch] $Docker,
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
if ([string]::IsNullOrWhiteSpace($ReplayDir)) {
    $ReplayDir = Join-Path $repoDirectory "run\config\ebsl\replays"
}

$url = "http://localhost:$Port"

if ($Docker) {
    $resolvedWorldDir = (Resolve-Path -LiteralPath $WorldDir).Path
    New-Item -ItemType Directory -Path $ReplayDir -Force | Out-Null
    $resolvedReplayDir = (Resolve-Path -LiteralPath $ReplayDir).Path
    $env:VIEWER_PORT = "$Port"
    $env:VIEWER_WORLD_DIR = $resolvedWorldDir
    $env:VIEWER_REPLAY_DIR = $resolvedReplayDir

    Write-Host "Serving pathfinder sim viewer with Docker at $url"
    Write-Host "Mounting worlds from $resolvedWorldDir to /workspace/run/saves"
    Write-Host "Saving replays to $resolvedReplayDir"
    Write-Host "Building image and starting isolated viewer server."
    Write-Host "Press Ctrl+C to stop the viewer server."

    if (-not $NoOpen) {
        Start-Job -ScriptBlock {
            param($ViewerUrl)
            Start-Sleep -Seconds 8
            Start-Process $ViewerUrl
        } -ArgumentList $url | Out-Null
    }

    docker compose up --build pathfinder-sim-viewer
    exit $LASTEXITCODE
}

Write-Host "Serving pathfinder sim viewer at $url"
Write-Host "Bound to $BindAddress for LAN/mobile testing."
Write-Host "Serving Java replay API from $ReplayDir"
Write-Host "Building and syncing viewer assets before launch."
Write-Host "Press Ctrl+C to stop the viewer server."

& .\gradlew.bat :tools:pathfinder-sim-viewer:check :tools:pathfinder-sim-server:processResources "-Pviewer.port=$Port" "-Pviewer.bindAddress=$BindAddress" "-Pviewer.replayDir=$ReplayDir" --console=plain
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

if (-not $NoOpen) {
    Start-Job -ScriptBlock {
        param($ViewerUrl)
        Start-Sleep -Seconds 2
        Start-Process $ViewerUrl
    } -ArgumentList $url | Out-Null
}

& .\gradlew.bat :tools:pathfinder-sim-server:bootRun "-Pviewer.port=$Port" "-Pviewer.bindAddress=$BindAddress" "-Pviewer.replayDir=$ReplayDir" --console=plain
