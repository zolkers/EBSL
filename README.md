# EBSL

[![Build](https://github.com/zolkers/EBSL/actions/workflows/build.yml/badge.svg?branch=dev)](https://github.com/zolkers/EBSL/actions/workflows/build.yml)
[![Quality Gate](https://img.shields.io/badge/quality%20gate-local%20checks-brightgreen)](#quality-gates)
[![Coverage](https://img.shields.io/badge/coverage-%E2%89%A5%2080%25-brightgreen)](#quality-gates)
[![Code Review](https://img.shields.io/badge/code%20review-required-blue)](CONTRIBUTING.md)
[![Conventional Commits](https://img.shields.io/badge/commits-conventional-brightgreen)](CONTRIBUTING.md#commit-conventions)
[![Java](https://img.shields.io/badge/java-21-orange)](#supported-target)
[![Minecraft](https://img.shields.io/badge/minecraft-1.21.11-62B47A)](#supported-target)
[![Fabric](https://img.shields.io/badge/fabric-%E2%89%A5%200.18.4-blue)](#supported-target)
[![License](https://img.shields.io/badge/license-GPL--3.0--or--later-blue)](LICENSE)

EBSL is a Fabric client-side Minecraft automation and workflow toolkit focused on pathfinding, movement execution, world interaction, and scriptable in-game workflows.

The project is currently being prepared for public open-source development. APIs and internal modules may still change quickly.

## Features

- Modular pathfinding with movement classification, path quality scoring, and execution recovery.
- Client-side overlays and ImGui tooling for navigation, scripts, analytics, packets, and module settings.
- Scriptable workflow graph and command system for repeatable automation tasks.
- Headless navigation runtime used by tests and non-Minecraft adapters.
- Local quality gates for coverage, source hygiene, and SonarQube integration.

## Supported Target

- Minecraft `1.21.11`
- Fabric Loader `>=0.18.4`
- Java `21`

## Ethical Use

EBSL is designed for automation and workflow visualization in single-player environments or in multiplayer environments where these tools are explicitly permitted.

You are responsible for ensuring that your use of this project does not violate server rules, terms of service, or community guidelines. Do not use EBSL to gain unfair advantages, disrupt other players, or bypass rules set by server administrators or platform providers.

## Build

On Windows:

```powershell
.\gradlew.bat check
.\gradlew.bat buildMod121Fabric
```

On macOS/Linux:

```bash
./gradlew check
./gradlew buildMod121Fabric
```

Build artifacts are written under `build/`.

## Run Locally

On Windows:

```powershell
.\gradlew.bat runClient121Fabric
```

On macOS/Linux:

```bash
./gradlew runClient121Fabric
```

The development run directory is `run/`, which is intentionally ignored by Git.

## Quality Gates

`check` runs the project-local quality gates:

- tests across Java modules
- aggregate JaCoCo coverage
- Sonar-style coverage threshold, currently `>= 80%`
- hardcoded fully-qualified Java name guard

Local SonarQube setup is documented in [docs/sonarqube-local.md](docs/sonarqube-local.md).

## Git Conventions

This repository uses Conventional Commits. See [CONTRIBUTING.md](CONTRIBUTING.md) for commit scopes, branch naming, and review expectations.

Install the local Git hooks after cloning:

On Windows:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\install-git-hooks.ps1
```

On macOS/Linux:

```bash
sh ./scripts/install-git-hooks.sh
```

## Repository Layout

- `modules/common/*`: platform-independent contracts, pathfinding, execution, navigation, and rendering APIs.
- `modules/app`: shared application features, UI, terminal commands, scripting, and tests.
- `modules/minecraft-1-21-11/common`: Minecraft-specific adapters shared by loaders.
- `modules/minecraft-1-21-11/fabric`: Fabric entrypoint and packaging.
- `docs`: project documentation.
- `scripts`: local setup helpers.

See [docs/architecture.md](docs/architecture.md) for module boundaries and extension contracts.

## License

EBSL is released under [GPL-3.0-or-later](LICENSE). Distributed forks and modified versions must remain open source under the same license terms.
