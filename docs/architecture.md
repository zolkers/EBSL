# Architecture

For day-to-day commands, simulator regression, viewer launch, Docker, Minecraft build artifacts, and Sonar, see
[developer-workflow.md](developer-workflow.md).

EBSL is organized around platform-independent contracts first, then Minecraft/Fabric adapters.

## Module Boundaries

- `modules/common/*`: reusable Java contracts and implementations with no direct Fabric dependency.
- `modules/common/api`: shared API surfaces for navigation, runtime, pathfinding, rendering, threading, events, analytics, and common settings.
- `modules/common/settings`: shared persistence for common pathfinder and scripting editor settings.
- `modules/common/analytics`: reusable analytics events and snapshots.
- `modules/common/automation`: bot task and aiming primitives that are not scripting-language concerns.
- `modules/common/pathfinder-core`: path search, movement classification, quality scoring, path processing, and diagnostics.
- `modules/common/pathfinder-execution`: movement executors and runtime path following helpers.
- `modules/common/navigation`: service-level navigation API and runtime adapters.
- `modules/mc/<minecraft-version>/common`: Minecraft-specific world and player adapters shared by loaders.
- `modules/mc/<minecraft-version>/fabric`: Fabric entrypoint, metadata, packaging, and final composition of the
  app runtime into the Minecraft adapter.
- `modules/tools/pathfinder-sim`: Java headless and Swing simulator used to run scenarios, import worlds, and export replay JSON.
- `modules/tools/pathfinder-sim-viewer`: static responsive PWA for desktop and Android/mobile replay inspection.

## Platform Shape

Minecraft version modules follow the same shape so new targets can be added without copying the EBSL platform:

- `mc/<version>/common` may depend on common contracts and Minecraft APIs, but not on `:app` or app-owned UI/registry
  packages. It exposes loader-side contracts such as application factories and UI bridges.
- `mc/<version>/fabric` is the loader composition layer. It is allowed to connect `EbslCore`, the ImGui overlay, Fabric
  APIs, and Minecraft version adapters because this module is the final packaged product.
- `gradle/ebsl-modules.gradle` keeps runtime common modules separate from application modules. The Fabric jar embeds
  both, but shared Minecraft code compiles only against the common runtime list.
- `localQuality` runs `architectureBoundaryVerification`, which fails if `mc/<version>/common` starts depending on app
  classes again or if common modules import Minecraft/Fabric classes.

## Pathfinder Contracts

Pathfinder behavior should be extended through explicit contracts instead of static helper duplication:

- `MovementTypeClassifier`: labels a transition as walk, step-up, jump, parkour, swim, and related movement types.
- `MovementCostModel`: maps movement types to risk and planning penalties.
- `NodeProcessor`: validates transitions and contributes search cost.
- `PathQualityMetric`: scores completed paths for diagnostics and planning feedback.

`PathfinderConfiguration` owns these contracts. A* search, path post-processing, and quality-aware costs should read from the configuration so they agree on movement semantics.

## Contribution Rules

- Keep Fabric and Minecraft imports out of platform-independent modules.
- Keep app feature registries out of `common:api`, `common:analytics`, and `common:settings`; app-owned modules/tasks adapt through app-side stores.
- Add new Minecraft targets under `modules/mc/<minecraft-version>` with the same loader submodule shape.
- Apply `gradle/mc-version.gradle` from each Minecraft target submodule so Java and loader versions stay version-owned.
- Prefer adding or replacing a contract implementation over branching inside core algorithms.
- When a new movement behavior is added, update classification, validation, execution, quality scoring, and tests together.

## Simulator Viewer Compatibility

The web simulator viewer is a viewer first. The Java simulator remains the authoritative runtime for Minecraft save
import, physics, pathfinding, metrics, and replay generation. Desktop and Android/mobile clients consume exported replay
JSON so they stay compatible with the same runs used by the Swing UI and headless regression checks.

Do not duplicate A*, Anvil loading, movement classification, or physics rules in the web viewer. If the viewer needs
live simulation controls, add a small server bridge around `tools:pathfinder-sim` and keep browsers as remote UIs.
Render metadata that depends on Minecraft or simulator semantics, including terrain category colors, should be emitted
by the Java replay export rather than reclassified in TypeScript.
- If two classes need the same movement decision, extract it into a shared contract instead of copying the rule.
