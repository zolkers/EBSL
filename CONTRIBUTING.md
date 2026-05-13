# Contributing

Thanks for helping make EBSL better. The project is still moving quickly, so small, focused changes are preferred.

## Development Workflow

1. Branch from `dev`.
2. Keep changes scoped to one concern.
3. Run `./gradlew check` before opening a pull request.
4. Open the pull request into `dev`.
5. Keep `master` reserved for stable public snapshots.

## Quality Checks

Run the local quality gate before submitting changes:

On Windows:

```powershell
.\gradlew.bat clean check
```

On macOS/Linux:

```bash
./gradlew clean check
```

If you have a local SonarQube instance configured, also run:

On Windows:

```powershell
.\gradlew.bat sonar
```

On macOS/Linux:

```bash
./gradlew sonar
```

Local SonarQube setup is documented in [docs/sonarqube-local.md](docs/sonarqube-local.md). Sonar is recommended for contributors and expected before merging larger code changes.

## Branch Names

Use short lowercase branch names:

- `feature/<topic>`
- `fix/<topic>`
- `refactor/<topic>`
- `docs/<topic>`
- `test/<topic>`
- `ci/<topic>`

## Conventional Commits

Commit messages must follow:

```text
type(scope): summary
```

Allowed types:

- `feat`: user-visible feature
- `fix`: bug fix
- `refactor`: internal code change without behavior change
- `test`: tests only
- `docs`: documentation only
- `ci`: CI or quality gate changes
- `build`: build system or dependency changes
- `chore`: repository maintenance
- `perf`: performance improvement
- `style`: formatting only
- `remove`: intentional deletion of code or assets

Examples:

```text
fix(pathfinder): stabilize step-up execution
refactor(imports): replace hardcoded qualifiers
ci(quality): enforce local quality gates
```

Use imperative, lowercase summaries and avoid trailing punctuation.

## Pull Requests

Every pull request should include:

- what changed
- why it changed
- how it was verified
- risks or follow-up work, if any

## Code Standards

- Prefer existing module boundaries and helper APIs.
- Keep Minecraft/Fabric imports out of platform-independent modules.
- Follow the module boundaries and extension contracts in [docs/architecture.md](docs/architecture.md).
- Treat [docs/architecture/api-surface.md](docs/architecture/api-surface.md) as the source of truth for public API, SPI, and implementation visibility.
- Do not commit generated files, local run data, IDE files, or `.codex-tmp/`.
- Avoid hardcoded fully-qualified Java type names outside imports.
- Keep tests focused on behavior and risk.

## Local Git Hooks

Install hooks once per clone:

On Windows:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\install-git-hooks.ps1
```

On macOS/Linux:

```bash
sh ./scripts/install-git-hooks.sh
```

The hook checks commit message format locally. CI and review still remain the source of truth.
