# Pathfinder Simulator Distribution

The simulator has three distribution targets with different responsibilities.

## Web/PWA Viewer

`tools:pathfinder-sim-viewer` is the lightweight replay viewer. It is a static TypeScript PWA and should stay focused on
loading exported replay JSON, rendering playback, and working on desktop or Android browsers.

Use it locally with:

```powershell
.\scripts\sim-viewer.ps1
```

The script builds the viewer, starts a local server, opens the browser, and binds to `0.0.0.0` so an Android device on
the same network can open `http://<pc-lan-ip>:8087`.

This target should not duplicate Anvil world loading, physics, goal resolution, or pathfinding. Those stay in Java.

## Desktop Simulator App

`tools:pathfinder-sim` is the source-of-truth simulator. It loads Minecraft worlds, resolves goals, runs pathfinding,
simulates movement, exports replay JSON, and provides the Swing UI.

Build a self-contained desktop app image with:

```powershell
.\gradlew.bat :tools:pathfinder-sim:packageDesktopAppImage
```

The output is written under:

```text
build\tools\pathfinder-sim\desktop
```

This is the right desktop packaging path because it keeps the Java simulator intact and ships a bundled runtime through
`jpackage`.

## Android Standalone App

A real Android app that loads a Minecraft world directly on-device is not the same as the current PWA. It needs native
file access to save folders, a local Anvil importer, goal resolution, pathfinding, simulation, and replay rendering.

The long-term shape should be:

- `common` / `common:navigation`: shared pathfinding and navigation logic
- `tools:pathfinder-sim`: desktop simulator and Java/Swing UI
- `tools:pathfinder-sim-viewer`: static replay viewer and PWA
- future `tools:pathfinder-sim-mobile`: Android app wrapper with a thin native shell and shared simulation APIs

Before adding the Android module, the simulator core should be split behind a UI-neutral service API:

- `SimulationRequest`: world source, start, goal, radius, timing, and output options
- `SimulationService`: runs a request and returns a `SimulationReport`
- `WorldSource`: synthetic world, Anvil folder, or future Android document-tree source
- `ReplayStore`: save/load local reports for replay history

Android then calls the service instead of reimplementing pathfinding in TypeScript. That keeps the mobile app compatible
with the desktop simulator and prevents two physics/pathfinding implementations from drifting apart.
