param(
    [int] $Runs = 20,
    [int] $MaxTicks = 600,
    [double] $MaxFinalDistance = 1.25,
    [int] $MaxStuckEvents = 5,
    [switch] $SaveReplay
)

$ErrorActionPreference = "Stop"

$scriptDirectory = Split-Path -Parent $MyInvocation.MyCommand.Path
$repoDirectory = Split-Path -Parent $scriptDirectory

$bundledJdk = Join-Path $repoDirectory ".gradle\ebsl-jdks\microsoft-jdk-21-x64"
if (-not $env:JAVA_HOME -and (Test-Path (Join-Path $bundledJdk "bin\java.exe"))) {
    $env:JAVA_HOME = $bundledJdk
}

if (-not $env:JAVA_HOME) {
    $localJdk = Join-Path $env:USERPROFILE ".jdks\ms-21.0.10"
    if (Test-Path (Join-Path $localJdk "bin\java.exe")) {
        $env:JAVA_HOME = $localJdk
    }
}

if ($env:JAVA_HOME) {
    $env:Path = "$(Join-Path $env:JAVA_HOME "bin");$env:Path"
}

Set-Location $repoDirectory

$saveReplayProperty = if ($SaveReplay) { "true" } else { "false" }
& .\gradlew.bat :tools:pathfinder-sim:regression `
    "-PsimRegressionRuns=$Runs" `
    "-PsimRegressionMaxTicks=$MaxTicks" `
    "-PsimRegressionMaxFinalDistance=$MaxFinalDistance" `
    "-PsimRegressionMaxStuckEvents=$MaxStuckEvents" `
    "-PsimRegressionSaveReplay=$saveReplayProperty" `
    --console=plain
exit $LASTEXITCODE
