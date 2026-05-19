# Pathfinder Simulator

`tools:pathfinder-sim` is a standalone headless simulator for EBSL pathfinding. It is not a Minecraft runtime module and has no Fabric or Minecraft dependency.

The simulator runs scripted Minecraft-like worlds against the shared pathfinder/navigation modules, then reports:

- final navigation status
- tick count
- planned node counts
- stuck ticks and stuck events
- final distance to goal
- sampled per-tick traces in JSON

## Run

On Windows:

```powershell
.\gradlew.bat :tools:pathfinder-sim:run --args="--json=build/pathfinder-sim/report.json"
```

Headless batch run:

```powershell
.\gradlew.bat :tools:pathfinder-sim:run --args="--headless --json=build/pathfinder-sim/report.json"
```

Run a single scenario:

```powershell
.\gradlew.bat :tools:pathfinder-sim:run --args="--scenario=parkour --max-ticks=900"
```

Open the graphical replay viewer:

```powershell
.\gradlew.bat :tools:pathfinder-sim:run
```

Import a Minecraft Anvil save around a start and goal:

```powershell
.\gradlew.bat :tools:pathfinder-sim:run --args="--mc-world=C:\Users\you\AppData\Roaming\.minecraft\saves\World --mc-start=383.5,64,44.5 --mc-goal=500,62,40 --mc-radius=5 --json=build/pathfinder-sim/mc-report.json"
```

Import the repo `run` save and let the simulator choose a valid player-side start plus a far reachable goal:

```powershell
.\gradlew.bat :tools:pathfinder-sim:run --args="--headless --scenario=minecraft --mc-world=run\saves\New --mc-diagnostics --mc-goal-search=64 --json=build/pathfinder-sim/mc-report.json"
```

The Minecraft importer resolves relative world paths from the simulator module and its parent folders. It also tolerates a truncated save folder prefix such as `run\saves\New` when the actual save is named `New World`, which keeps PowerShell/Gradle quoting friction out of normal use.

## Scenarios

The default catalogue includes:

- `flat_sprint_24`
- `wall_bypass_gap`
- `parkour_gap_3`
- `stair_climb_two`
- `ladder_column`

Failures are useful. A failed scenario records where the current planning/following/physics model disagrees, gets stuck, falls away, or terminates too early.

## Packages

- `app`: CLI entry point
- `cli`: argument parsing
- `core`: simulation runner and stuck detection
- `replay`: per-tick replay data and reports
- `scenario`: built-in synthetic worlds
- `ui`: Swing replay viewer
- `world.minecraft`: minimal Anvil `.mca` importer
