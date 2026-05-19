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

Run a single scenario:

```powershell
.\gradlew.bat :tools:pathfinder-sim:run --args="--scenario=parkour --max-ticks=900"
```

## Scenarios

The default catalogue includes:

- `flat_sprint_24`
- `wall_bypass_gap`
- `parkour_gap_3`
- `stair_climb_two`
- `ladder_column`

Failures are useful. A failed scenario records where the current planning/following/physics model disagrees, gets stuck, falls away, or terminates too early.
