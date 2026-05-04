# Swim Refactor + PathExecutor Cleanup

## Problem

SWIM moveType is never assigned by either `AStarPathfinder.inferMoveType` or
`PathGeometry.inferMoveType` — both inspect only dy/dx/dz offsets, never world state.
Every node in water gets tagged WALK/FALL/STEP_UP. `SwimMovement` is registered in
the registry but is dead code at execution time.

The fallback in `WalkMovementController.applyWaterMovement` takes over: jump if
waypoint is above, otherwise nothing. Minecraft buoyancy does the rest → bot always
floats to surface regardless of the planned path.

---

## Task 1 — Fix Swim Node Tagging

**Where**: `WalkPathProcessor.toNodeList()` and `WalkPathProcessor.insertIntermediates()`

**How**: After inferring moveType from geometry, check if the position (or position-1 Y)
is water via the `WalkabilityChecker` already passed into `processWalkPath`. If so,
retag as `SWIM`.

```
checker.isWater(x, y, z) || checker.isWater(x, y-1, z)  →  moveType = SWIM
```

Guard: checker may be null — fall through to geometry-inferred type in that case.
Intermediates in `insertIntermediates` inherit `from.moveType`, but explicitly retag
if checker confirms water at their position too.

---

## Task 2 — Simplify SwimMovement (Baritone-style)

**Delete entirely**:
- `resolveSurfaceY`
- `traceWaterObstruction` + `Obstruction` record
- `computeTargetFeetY`
- `isSurfaceWaypoint`
- Constants: `SURFACE_FEET_OFFSET`, `SUBMERGED_FEET_OFFSET`, `SURFACE_LOCK_RANGE`, `LOOK_RAY_DIST`

**New `handleWaterMovement` logic**:

```
int waypointY  = waypoint.position.flooredY()
int playerY    = floor(playerPos.y)

needUp   = waypointY >= playerY || isHeadUnderWater(mc, playerPos)
needDown = waypointY < playerY - 1

jump  = needUp && !needDown
sneak = needDown && !needUp
sprint = true
```

No surface Y scan, no ray tracing. Minecraft buoyancy + sprint handles the rest.
`isHeadUnderWater` and `isWaterColumn` are kept. `handleJump` override is kept
(releases jump in water so the SWIM handler owns vertical control entirely).

---

## Task 3 — PathExecutor Config Consolidation

**Problem**: `PathExecutor` re-declares all config fields that `WalkExecutionOptions`
already owns (`allowReplan`, `allowJumps`, `allowRotation`, `exactGoalCentering`,
`stickySneakDistance`, `sneakLatched`, `preciseGoalTolerance`, `goalCenterX`,
`goalCenterZ`). The current flow is `WalkExecutionOptions.applyTo(executor)` → 8
individual setters → PathExecutor re-stores locally. Backwards.

**Fix**: PathExecutor holds a `WalkExecutionOptions` reference after `start()`.
All reads go through it. Setters on PathExecutor are removed; callers configure
`WalkExecutionOptions` directly. `applyTo()` is removed.

---

## Task 4 — PathExecutor `tick()` Readability

Extract the following from the 175-line `tick()`:

- **`isAtGoal(Vec3)`** — replace the anonymous `{ }` block (lines 217-226)
- **`tickGoalCoasting(Minecraft, Vec3)`** — the precise coasting section
- **`tickRecovery(...)`** — PathProgressSnapshot + RecoveryDecision block

`debugTick` instance field → replaced with a `lastDebugTime` long checked against
`System.currentTimeMillis()` to avoid field noise.
