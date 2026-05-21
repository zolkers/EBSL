param(
    [string] $WorldDir = "run\saves\New World",
    [string] $Start = "386,61,42",
    [string] $Goal = "500,61,40",
    [int] $Runs = 3,
    [int] $MaxTicks = 1200,
    [int] $RadiusChunks = 8,
    [int] $GoalSearchBlocks = 160,
    [double] $MaxFinalDistance = 2.0,
    [int] $MaxStuckEvents = 12,
    [string] $MaxHeap = "4g",
    [switch] $NoReplay
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

$saveReplay = if ($NoReplay) { "false" } else { "true" }
& .\gradlew.bat :tools:pathfinder-sim:minecraftRegression `
    "-PsimMinecraftWorld=$WorldDir" `
    "-PsimMinecraftStart=$Start" `
    "-PsimMinecraftGoal=$Goal" `
    "-PsimMinecraftRuns=$Runs" `
    "-PsimMinecraftMaxTicks=$MaxTicks" `
    "-PsimMinecraftRadiusChunks=$RadiusChunks" `
    "-PsimMinecraftGoalSearchBlocks=$GoalSearchBlocks" `
    "-PsimMinecraftMaxFinalDistance=$MaxFinalDistance" `
    "-PsimMinecraftMaxStuckEvents=$MaxStuckEvents" `
    "-PsimMinecraftMaxHeap=$MaxHeap" `
    "-PsimMinecraftSaveReplay=$saveReplay" `
    --console=plain
exit $LASTEXITCODE
