# Pathfinder Simulator Server

`tools:pathfinder-sim-server` is the local Spring Boot backend for the web replay viewer.

It keeps the browser thin:

- Java serves the replay catalogue and replay JSON files from the simulator persistence directory.
- Java serves the compiled TypeScript viewer from Spring Boot static resources.
- The TypeScript app renders and controls the replay, but does not own simulator rules.

Run it through the shared launcher:

```powershell
.\scripts\sim-viewer.bat
```

Run it through Docker with isolated assets and process state:

```powershell
docker compose up --build pathfinder-sim-viewer
```

The Docker service exposes the same API on `http://localhost:8087`. Host worlds must be mounted into the container;
the default compose file maps `run\saves` to `/workspace/run/saves`.

Or directly:

```powershell
.\gradlew.bat :tools:pathfinder-sim-server:bootRun
```

Useful properties:

```powershell
.\gradlew.bat :tools:pathfinder-sim-server:bootRun `
  -Pviewer.port=8090 `
  -Pviewer.bindAddress=127.0.0.1 `
  -Pviewer.replayDir=run\pathfinder-replays
```

API:

- `GET /api/health`
- `GET /api/replays`
- `GET /api/replays/{fileName}`
- `GET /api/goals`
- `POST /api/simulations/minecraft`

The HTTP layer delegates to `PathfinderSimApi`; simulator behavior should live in the Java simulator API, not in Spring
controllers.

Example route request:

```json
{
  "worldDirectory": "run\\saves\\New World",
  "startX": 386.5,
  "startY": 61.0,
  "startZ": 42.5,
  "goalId": "walk",
  "goalValues": { "x": 500, "y": 61, "z": 40 },
  "maxTicks": 600,
  "radiusChunks": 5,
  "goalSearchBlocks": 96,
  "saveReplay": true
}
```
