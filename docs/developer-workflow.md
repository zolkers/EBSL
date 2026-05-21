# EBSL Developer Workflow

This page is the short operational checklist for local maintenance.

For the long-form architecture roadmap, use [roadmap.md](roadmap.md).

## Java

Use the repo scripts first. They pick up the bundled JDK when it exists, and fall back to the local IDE JDK on Windows.

When running Gradle manually from a fresh terminal, set Java first if needed:

```powershell
$env:JAVA_HOME="$env:USERPROFILE\.jdks\ms-21.0.10"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
```

Minecraft 1.21.x modules currently compile on the shared Java 21 toolchain. Minecraft 1.26.1+ work should use the `modules/mc/<version>` layout and can move that version line to Java 25 without changing the common tool modules.

## Pathfinder Simulator

Launch the desktop/Swing simulator:

```powershell
.\gradlew.bat :tools:pathfinder-sim:run
```

Run the repeatable headless regression suite:

```powershell
.\scripts\sim-regression.bat
```

Run the real-world Minecraft regression suite, using the current local `run/saves/New World` route by default:

```powershell
.\scripts\sim-mc-regression.bat
```

Default route:

```text
start=386,61,42
goal=500,61,40
world=run/saves/New World
```

Override any route without editing code:

```powershell
.\scripts\sim-mc-regression.bat -WorldDir "D:\Minecraft\saves\My World" -Start "386,61,42" -Goal "500,62,40" -Runs 5
```

Useful overrides:

```powershell
.\scripts\sim-regression.bat -Runs 50 -MaxTicks 900 -MaxFinalDistance 1.25 -MaxStuckEvents 5
```

The Gradle task behind the script is:

```powershell
.\gradlew.bat :tools:pathfinder-sim:regression -PsimRegressionRuns=20 -PsimRegressionMaxTicks=600
```

The default replay directory is:

```text
run/config/ebsl/replays
```

Pass `-SaveReplay` to `sim-regression.bat` only when you want to keep every regression replay.

## Web Viewer

Launch the Java-backed viewer:

```powershell
.\scripts\sim-viewer.bat
```

The viewer defaults to:

```text
http://localhost:8087
```

Use a custom world root and replay root:

```powershell
.\scripts\sim-viewer.bat -WorldDir "D:\Minecraft\saves" -ReplayDir "D:\EBSL\replays"
```

Run the same viewer through Docker:

```powershell
.\scripts\sim-viewer.bat -Docker -WorldDir "D:\Minecraft\saves"
```

For mobile/LAN testing, keep the default bind address `0.0.0.0`, then open the machine IP and port from the phone browser.

## Minecraft Build

Build every configured Minecraft Fabric jar:

```powershell
.\gradlew.bat buildMinecraftMods
```

Artifacts are written under each versioned Fabric module:

```text
build/mc/<minecraft-version>/fabric/libs
```

GitHub Actions uploads all matching jars from `build/mc/**/fabric/libs`.

## Quality

Run the local quality bundle:

```powershell
.\gradlew.bat localQuality
```

Run local SonarQube after the server and token are configured:

```powershell
.\scripts\setup-sonar-token.ps1 -Token "squ_..." -HostUrl "http://localhost:9000"
.\gradlew.bat sonar sonarQualityGate
```

`localQuality` is the fast local truth. The remote/local SonarQube Quality Gate still needs a live server and a valid token.
