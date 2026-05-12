# Module architecture

EBSL exposes small Gradle modules under `modules/`. Each module owns its own
`src/main/java` tree. Avoid reintroducing cross-module `sourceSets` filters:
the folder layout is the architectural boundary.

## Module groups

### Core contracts

`common:base`
: Math primitives, logging, registries, threading, and setting value types.

`common:events`
: Common event bus and packet capture domain events. Depends on `base`.

`common:world-api`
: Entity/world snapshots, block selectors, and the world/player/entity layer
interfaces under `fr.riege.ebsl.common.world.layer` required by non-Minecraft
pathfinding consumers. Depends on `base`.

`common:render-api`
: Render command/value primitives only. Depends on `base`.

`common:platform-api`
: Host integration interfaces such as input, physics, command, event bus, UI,
storage, and render layers. Depends on `base`, `events`, and `render-api`.

### Pathfinder

`common:pathfinder-core`
: Pure path search, goals, movement evaluation, walkability, settings, path
results, diagnostics hooks, and reusable path data structures. It must stay free
of UI, terminal, Minecraft, platform layers, runtime input execution, and debug
rendering. A server that only wants to compute mob paths should start here.

`common:pathfinder-execution`
: Runtime path following, rotation, movement controllers, recovery, input
application, and movement executors. Depends on `pathfinder-core` and
`platform-api`.

`common:pathfinder-debug`
: Path visualizer state and render helpers. Depends on `pathfinder-core` and
`render-api`.

### Navigation and application

`common:navigation`
: Higher-level navigation services and headless/server/entity runtimes built on
pathfinder core/execution and platform contracts.

`app`
: Full client-side application aggregate: public facade, modules, scripting,
terminal, UI, settings store, analytics, service wiring, and `EbslCore`.

## Dependency direction

```text
base
  -> events
  -> world-api
  -> render-api

events + render-api
  -> platform-api

base + world-api
  -> pathfinder-core
  -> pathfinder-debug

pathfinder-core + platform-api
  -> pathfinder-execution
  -> navigation
  -> app
```

The direction should stay one-way. If a lower module needs behavior from a
higher module, move a small contract down or use a diagnostics/service hook
instead of importing the higher module.

## Ownership rules

- A reusable module owns its own `src/main/java` tree.
- Avoid empty aggregate modules in source. If publication needs a convenience
  bundle later, prefer Gradle metadata or a BOM over a fake source module.
- `pathfinder-core` is computation only: no UI, no terminal, no render layer, no
  input layer, no Minecraft imports.
- Runtime input and camera behavior belong in `pathfinder-execution`.
- Visualization belongs in `pathfinder-debug`.
- Full mod features such as terminal commands, scripting nodes, ImGui panels,
  modules, analytics storage, and application bootstrap belong in `app` until
  they are split into dedicated feature modules.
- Minecraft adapter builds consume `rootProject.ext.ebslRuntimeClassModules`
  from `gradle/ebsl-modules.gradle`; do not duplicate the module list by hand.
- Shared Java modules use `gradle/java-module.gradle`, which rejects
  Minecraft/Fabric imports. `pathfinder-core` also rejects higher-level
  pathfinder, platform, render, and feature imports during compilation.
