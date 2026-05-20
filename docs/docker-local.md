# Local Docker Services

EBSL uses Docker only for reproducible development services. The Minecraft client, Gradle builds, and Java toolchains still run on the host because Fabric/Loom, GPU/UI debugging, and local IDE workflows are much easier to keep stable that way.

## Services

The repository currently provides:

- `sonarqube`: local static analysis server at `http://localhost:9000`
- `sonarqube-db`: PostgreSQL data store for SonarQube

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

## What Belongs In Docker

Good candidates:

- SonarQube and its database
- future documentation preview servers
- future mock API services
- future benchmark result stores or dashboards
- optional observability services for simulator profiling

Poor candidates:

- Minecraft client runtime
- Fabric run tasks
- IDE state
- user saves under `run/`
- secrets or personal Sonar tokens

## Local Configuration

The compose file provides safe local defaults for PostgreSQL. Override them through environment variables or a local `.env` file if needed:

```text
SONAR_POSTGRES_USER=sonarqube
SONAR_POSTGRES_PASSWORD=sonarqube
SONAR_POSTGRES_DB=sonarqube
```

Do not commit personal Sonar tokens. Use `scripts/setup-sonar-token.ps1` or `scripts/setup-sonar-token.sh`; they write the token to your user Gradle properties outside the repository.
