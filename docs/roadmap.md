# EBSL Architecture Roadmap

This roadmap tracks what remains to turn EBSL into a durable platform-style project. Keep it practical: every item
must end with a validation command, a clear owner module, and a measurable acceptance condition.

## Current Baseline

- `common:plugin-api` exists and defines extension points for runtime, UI, scripting, entity brains, and pathfinder regression.
- Minecraft version modules follow the `mc/<version>/common` + `mc/<version>/fabric` shape.
- `mc/<version>/common` no longer depends on `:app`.
- CI builds all configured Fabric targets through `buildMinecraftMods`.
- SonarQube currently has `0` open issues and a green Quality Gate.

## Priority 1: Split The App Runtime

Goal: make `:app` a composition/front-end module instead of the owner of platform runtime concepts.

Work:

- Create a runtime-oriented module, likely `common:app-runtime` or `common:runtime`.
- Move bootstrap-neutral pieces out of `:app`, starting with the parts of `EbslCore` that only install services,
  event hooks, settings, diagnostics, and rendering ticks.
- Keep ImGui rendering and concrete app UI inside `:app` until a dedicated UI module exists.
- Make the Fabric bridge instantiate the runtime through a stable interface instead of directly knowing all app classes.

Acceptance:

- `EbslCore` becomes a thin compatibility facade or moves into the runtime module.
- `:app` can be described as UI plus app-owned feature composition, not core platform.
- `mc:*:fabric` depends on app composition only at the final loader layer.

Validation:

```powershell
.\gradlew.bat buildMinecraftMods localQuality
```

## Priority 2: Extract Feature Registries

Goal: replace static app-owned registries with extension-backed registries.

Work:

- Extract command, module, task, scripting, and UI registry contracts into common modules.
- Adapt `FeatureRegistries` to read from `common:plugin-api` contributions.
- Keep legacy static accessors temporarily as compatibility shims.
- Stop adding new features directly to `FeatureRegistries`; new features should contribute through extension points.

Acceptance:

- New command/module/scripting contributions can be registered without editing a central static catalog.
- `FeatureRegistries` no longer owns every feature type directly.
- Tests prove deterministic contribution order and duplicate handling.

Validation:

```powershell
.\gradlew.bat :common:plugin-api:test :app:test localQuality
```

## Priority 3: Separate UI Core From ImGui

Goal: make UI state and layout reusable by ImGui, web, desktop, or future Android shells.

Work:

- Move UI state, layout geometry, panel metadata, and view models into a non-ImGui module.
- Keep ImGui draw calls in an ImGui-specific module.
- Replace direct app UI references from platform adapters with small bridge contracts.
- Use `EbslUiContribution` for panel metadata and let frontends render their own implementation.

Acceptance:

- `UiRect`, view tabs, and UI state are not tied to ImGui packages.
- `CommonImGuiOverlay` becomes an adapter over UI core models.
- Future web/mobile viewers can consume UI metadata without Java ImGui dependencies.

Validation:

```powershell
.\gradlew.bat :app:compileJava buildMinecraftMods localQuality
```

## Priority 4: Make Scripting Truly n8n-like

Goal: make the graph scripting model independent, typed, extensible, and suitable for generated editors.

Work:

- Promote graph model, typed ports, node metadata, expression fields, and validation into the canonical scripting API.
- Replace line-oriented assumptions with graph-first execution.
- Add async branch execution semantics, switch nodes, parallel branches, selector/sequence nodes, and typed data ports.
- Use `EbslGraphScriptBuilder` as a first-class Java authoring API and keep it aligned with persisted graph documents.

Acceptance:

- A graph can be built entirely from Java API, serialized, validated, and executed.
- Node metadata can generate an editor palette without hardcoded UI knowledge.
- Multi-output nodes and typed data connections are covered by tests.

Validation:

```powershell
.\gradlew.bat :common:scripting:test localQuality
```

## Priority 5: Entity Brain Integration

Goal: let any entity attach an EBSL workflow graph as its brain.

Work:

- Formalize `EntityBrain -> ScriptingGraphRuntime -> EntityNavigationAgent`.
- Add brain registry contributions through `EbslEntityBrainContribution`.
- Add memory, blackboard, sensors, navigation status, and last path metrics to the brain context.
- Add graph templates for common behaviors: patrol, follow, escape, mine, recover.

Acceptance:

- An entity can be created with an `EntityNavigationAgent` and a graph-backed `EntityBrain`.
- Brain tick execution can drive navigation and react to status.
- Tests cover memory, graph tick, stop/recovery, and navigation command dispatch.

Validation:

```powershell
.\gradlew.bat :common:scripting:test :common:navigation:test localQuality
```

## Priority 6: Pathfinder Execution Consistency

Goal: make movement execution reliable enough to judge with repeated simulation, not only with one successful route.

Work:

- Build a stability regression mode that runs each route many times and reports min, mean, max, p95, and variance.
- Record lateral error, backward ticks, recovery count, stuck cause, segment progress, and jump timing.
- Feed execution risk back into path scoring: corridor lookahead, bad turn penalties, overshoot risk, and stuck risk.
- Improve recovery: backoff, realignment, local replan, jump retry, and clean abandon for impossible moves.

Acceptance:

- A real-world route can be run `N` times and fail on high variance even when every run reaches the goal.
- Replays explain why ticks were lost.
- Pathfinder scoring prefers paths that execute consistently, not only geometrically short paths.

Validation:

```powershell
.\scripts\sim-mc-regression.bat -Runs 10 -NoReplay
```

## Priority 7: Minecraft Physics Fidelity

Goal: make the simulator close enough to Minecraft that pathfinder decisions transfer to the real client.

Work:

- Expand the physics block registry for ice, honey, soul sand, liquids, ladders, vines, scaffolding, stairs, slabs,
  carpets, fences, collision shapes, and edge cases.
- Model exact sprint timing, acceleration, inertia, jump arc, friction, and input release/hold behavior.
- Track float/double rounding differences and client/server tick timing.
- Keep all block categories enum/registry-driven, not string-hardcoded in random mappers.

Acceptance:

- Each special block type has a dedicated physics profile and test fixture.
- Movement traces can compare expected vs simulated position per tick.
- The simulator can mark unsupported physics explicitly instead of silently pretending to be 1:1.

Validation:

```powershell
.\gradlew.bat :tools:pathfinder-sim:test localQuality
```

## Priority 8: Real-World Regression Matrix

Goal: make real Minecraft saves part of the pathfinder quality loop.

Work:

- Add a route catalog for local worlds with named cases and tags.
- Support route classes: flat, diagonal, corridor, climb, descent, water, edge, parkour, dense obstacles.
- Persist failing replays automatically under `run/config/ebsl/replays` unless overridden.
- Add comparison reports across repeated runs.

Acceptance:

- The 386,61,42 -> 500,61,40 world route is one named case, not a one-off command.
- Bad runs produce replay files and metric summaries automatically.
- The regression suite can run headless in CI when a world fixture is available.

Validation:

```powershell
.\scripts\sim-mc-regression.bat -Runs 3
```

## Priority 9: Desktop And Mobile Distribution

Goal: make the simulator/viewer available as clean desktop and Android-friendly shells without duplicating backend logic.

Work:

- Keep Java as the authoritative simulator/backend.
- Package web viewer as PWA for mobile browser usage.
- Investigate desktop wrapper only after API boundaries are stable.
- Keep Android standalone optional unless world loading and local file access can be handled cleanly.

Acceptance:

- Desktop users can launch Swing or web viewer with scripts.
- Mobile users can open the PWA against the Java backend on LAN.
- No pathfinding, physics, or world loading logic is duplicated in TypeScript.

Validation:

```powershell
.\scripts\sim-viewer.bat
```

## Priority 10: Quality, CI, And Sonar Discipline

Goal: keep the project clean while it grows.

Work:

- Keep SonarQube at `0` open issues.
- Keep `localQuality` as the local source of truth before commits.
- Expand architecture checks module by module.
- Add CI matrix jobs when new Minecraft versions are added.
- Add docs for every new module boundary before the boundary becomes relied upon.

Acceptance:

- Every architecture refactor ends with a commit and green `localQuality`.
- Sonar Quality Gate stays green.
- CI uploads jars for every configured Fabric target.

Validation:

```powershell
.\gradlew.bat buildMinecraftMods localQuality
.\gradlew.bat sonarQualityGate
```

## Next Recommended Slice

Start with Priority 1 and Priority 2 together:

1. Create `common:runtime`.
2. Move non-UI `EbslCore` responsibilities into it.
3. Convert `FeatureRegistries` into extension-backed compatibility accessors.
4. Keep `:app` as the concrete ImGui/app composition layer.

This slice creates the largest architectural gain while keeping the blast radius understandable.
