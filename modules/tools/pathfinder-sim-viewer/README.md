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

Serve it from the repo:

```powershell
python -m http.server 8087 -d build\tools\pathfinder-sim-viewer\webapp
```

Open `http://localhost:8087` on desktop, or `http://<pc-lan-ip>:8087` from an Android phone on the same network.

## Workflow

Generate a compatible replay:

```powershell
.\gradlew.bat :tools:pathfinder-sim:run --args="--headless --scenario=minecraft --mc-world=run\saves\New --mc-start=386,61,42 --mc-goal=500,61,40 --mc-radius=5 --json=build/pathfinder-sim/mc-report.json"
```

Then open the JSON file in the viewer.

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
