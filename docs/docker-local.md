# Local Docker Services

EBSL uses Docker only for reproducible development services. The Minecraft client, Gradle builds, and Java toolchains still run on the host because Fabric/Loom, GPU/UI debugging, and local IDE workflows are much easier to keep stable that way.

## Services

The repository currently provides:

- `sonarqube`: local static analysis server at `http://localhost:9000`
- `sonarqube-db`: PostgreSQL data store for SonarQube
- `pathfinder-sim-viewer`: isolated Spring Boot simulator viewer at `http://localhost:8087`

Start the default local services from the repository root:

```powershell
docker compose up -d
```

Stop services while keeping volumes:

```powershell
docker compose stop
```

Remove containers and local service data:

```powershell
docker compose down -v
```

## Pathfinder Simulator Viewer

Use Docker when the host browser keeps showing stale viewer assets or when you want a clean server process:

```powershell
docker compose up --build pathfinder-sim-viewer
```

The shared launchers can start the same Docker service:

```powershell
.\scripts\sim-viewer.bat -Docker
```

```bash
sh ./scripts/sim-viewer.sh --docker
```

Open:

```text
http://localhost:8087
```

The container builds the TypeScript viewer, syncs the Spring Boot static resources, and then starts the Java simulator
server. That makes the served HTML deterministic: `Open world`, the world browser, and the goal list come from the
current source tree in the image.

Docker can only browse host folders that are mounted into the container. By default, the compose service mounts:

```text
./run/saves -> /workspace/run/saves
```

So choose `/workspace/run/saves/<world>` from the web UI. To use another Minecraft saves directory, add a bind mount in
a local compose override or pass it through the launchers:

```powershell
.\scripts\sim-viewer.bat -Docker -WorldDir "C:\Users\<you>\AppData\Roaming\.minecraft\saves"
```

```bash
sh ./scripts/sim-viewer.sh --docker --world-dir "$HOME/.minecraft/saves"
```

Compose override example:

```yaml
services:
  pathfinder-sim-viewer:
    volumes:
      - "C:/Users/<you>/AppData/Roaming/.minecraft/saves:/minecraft-saves:ro"
```

Then choose `/minecraft-saves/<world>` in the viewer. Replays are persisted on the host by default:

```text
./run/config/ebsl/replays -> /data/replays
```

## What Belongs In Docker

Good candidates:

- SonarQube and its database
- isolated simulator viewer runs for stale-process debugging
- future documentation preview servers
- future mock API services
- future benchmark result stores or dashboards
- optional observability services for simulator profiling

Poor candidates:

- Minecraft client runtime
- Fabric run tasks
- IDE state
- unmapped personal save folders
- secrets or personal Sonar tokens

## Local Configuration

The compose file provides safe local defaults for PostgreSQL. Override them through environment variables or a local `.env` file if needed:

```text
SONAR_POSTGRES_USER=sonarqube
SONAR_POSTGRES_PASSWORD=sonarqube
SONAR_POSTGRES_DB=sonarqube
```

Do not commit personal Sonar tokens. Use `scripts/setup-sonar-token.ps1` or `scripts/setup-sonar-token.sh`; they write the token to your user Gradle properties outside the repository.
