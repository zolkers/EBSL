# Architecture

EBSL is organized around platform-independent contracts first, then Minecraft/Fabric adapters.

## Module Boundaries

- `modules/common/*`: reusable Java contracts and implementations with no direct Fabric dependency.
- `modules/common/pathfinder-core`: path search, movement classification, quality scoring, path processing, and diagnostics.
- `modules/common/pathfinder-execution`: movement executors and runtime path following helpers.
- `modules/common/navigation`: service-level navigation API and runtime adapters.
- `modules/minecraft-1-21-11/common`: Minecraft-specific world and player adapters shared by loaders.
- `modules/minecraft-1-21-11/fabric`: Fabric entrypoint, metadata, and packaging only.

## Pathfinder Contracts

Pathfinder behavior should be extended through explicit contracts instead of static helper duplication:

- `MovementTypeClassifier`: labels a transition as walk, step-up, jump, parkour, swim, and related movement types.
- `MovementCostModel`: maps movement types to risk and planning penalties.
- `NodeProcessor`: validates transitions and contributes search cost.
- `PathQualityMetric`: scores completed paths for diagnostics and planning feedback.

`PathfinderConfiguration` owns these contracts. A* search, path post-processing, and quality-aware costs should read from the configuration so they agree on movement semantics.

## Contribution Rules

- Keep Fabric and Minecraft imports out of platform-independent modules.
- Prefer adding or replacing a contract implementation over branching inside core algorithms.
- When a new movement behavior is added, update classification, validation, execution, quality scoring, and tests together.
- If two classes need the same movement decision, extract it into a shared contract instead of copying the rule.
