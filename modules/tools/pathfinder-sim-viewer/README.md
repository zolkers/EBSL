# Pathfinder Simulator Viewer

`tools:pathfinder-sim-viewer` is a responsive desktop and Android/mobile replay viewer for JSON files exported by
`tools:pathfinder-sim`.

It is intentionally separate from the Java/Swing simulator:

- the Java simulator remains the source of truth for world loading, pathfinding, physics, metrics, and replay export
- the web viewer consumes the existing `SimulationReport.toJson` schema
- the app is a static PWA, so it can run from a desktop browser or Android browser and work offline after first load

## Run Locally

Package and validate the app:

```powershell
.\gradlew.bat :tools:pathfinder-sim-viewer:check :tools:pathfinder-sim-viewer:assemble
```

Build and serve it from the repo:

```powershell
.\scripts\sim-viewer.bat
```

PowerShell entrypoint:

```powershell
.\scripts\sim-viewer.ps1
```

Unix shell entrypoint:

```bash
sh ./scripts/sim-viewer.sh
```

The scripts configure the bundled JDK and Node runtime when they are available locally, copy the Java simulator replay
catalogue into the served app, open the browser, and serve the viewer on `0.0.0.0` so Android devices on the same
network can connect.

You can also call the Gradle task directly when your environment is already configured:

```powershell
.\gradlew.bat :tools:pathfinder-sim-viewer:serve
```

Open `http://localhost:8087` on desktop, or `http://<pc-lan-ip>:8087` from an Android phone on the same network.

Use another port when needed:

```powershell
.\scripts\sim-viewer.bat -Port 8090
```

Limit the server to this PC only when needed:

```powershell
.\scripts\sim-viewer.bat -BindAddress 127.0.0.1
```

Serve a custom Java replay directory:

```powershell
.\scripts\sim-viewer.bat -ReplayDir run\pathfinder-replays
```

## Workflow

Generate a compatible replay:

```powershell
.\gradlew.bat :tools:pathfinder-sim:run --args="--headless --scenario=minecraft --mc-world=run\saves\New --mc-start=386,61,42 --mc-goal=500,61,40 --mc-radius=5 --json=build/pathfinder-sim/mc-report.json"
```

The Java simulator also persists replay JSON files and an `index.json` under `%USERPROFILE%\.ebsl\pathfinder-sim\replays`
by default. Restart `scripts\sim-viewer.bat` after generating new replays to refresh the saved replay list.

## Controls

- desktop left drag / mobile one finger drag: pan
- desktop wheel / mobile pinch: zoom
- desktop middle drag: rotate 3D
- scrubber: replay frame
- play: 20 TPS playback
- `2D / 3D`: switch between top-down and isometric replay rendering

## Code Ownership

The viewer deliberately avoids duplicating simulator rules. Terrain colors come from the Java replay export as `rgb`
values derived from `ReplayBlockKind`; the TypeScript layer only projects and draws replay data.
