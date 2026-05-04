# Parkour Pathfinding Plan

## Goal

Parkour must become an explicit movement family, not a long horizontal neighbor with a
late jump press. The pathfinder should only offer a parkour edge when a planner can prove
that the player can reach the landing with the required approach, headroom, support, and
momentum.

## References

- Baritone `Moves`: explicit `PARKOUR_*` moves and separate `ASCEND_*` moves.
- Baritone `MovementParkour`: evaluates candidate jumps by simulating/timing the movement
  instead of accepting every 2-4 block offset.
- Baritone `MovementAscend`: validates jump support, start headroom, destination clearance,
  and delays jump until alignment is good.
- Minecraft Parkour Wiki `Movement Formulas`, `Vertical Movement Formulas`, and `MathBot`:
  tick-based horizontal/vertical formulas, momentum, sprint jump, and 45-degree movement.
- Minecraft Parkour Wiki `Parkour Nomenclature` and `Timings`: terminology for momentum,
  force momentum, headhitter timing, and edge timing.

## Movement Families

- `STEP_UP`: normal cardinal `+1y` movement. Needs jump support and start headroom.
- `PARTIAL_ASCENT`: slab/stair-like support. Usually should not press jump; entering a
  bottom slab/stair line is closer to walking than parkour.
- `PARKOUR_FLAT`: same floor level, landing distance 2-4 blocks away.
- `PARKOUR_DOWN`: landing lower than start. Easier horizontally but needs safe landing and
  no collision on the falling arc.
- `PARKOUR_UP`: landing higher than start. Very restricted; most 4-block variants are not
  feasible without special setups.
- `MOMENTUM_SETUP`: intentionally run/edge-align before jumping. This is a pre-move, not a
  different physics rule.
- `HEADHITTER_MOMENTUM`: low ceiling setup that creates extra horizontal speed. This should
  be opt-in later because it needs precise timing and can easily bonk.
- `NEO`: around-corner jump. Should be a separate movement because it needs side clearance
  and yaw/strafe planning.

## Feasibility Model

Use a simulator, not a fixed distance table.

Inputs:

- Start support block, landing support block, and their floor heights.
- Horizontal delta from takeoff edge to landing box.
- Vertical delta from takeoff floor to landing floor.
- Approach distance available before takeoff.
- Whether sprint is available.
- Whether headroom allows a normal jump or forces a headhitter strategy.
- Block slipperiness at takeoff and during run-up.
- Bounding box clearance along the swept path.

Output:

- `feasible`: can this movement be executed from the current path state?
- `requiredApproachTicks`: how long to build speed before jumping.
- `jumpTick`: when to press jump relative to the edge.
- `targetYaw`: direct, 45-degree, or curved/neo trajectory.
- `requiredInputs`: forward/sprint/jump/strafe/sneak.
- `risk`: conservative penalty for tight timing or low margins.

Physics sketch:

- Simulate per tick at 20 TPS.
- Vertical starts around the vanilla jump impulse and then applies gravity/drag each tick.
- Horizontal velocity is updated from current input acceleration, sprint state, friction,
  and sprint-jump impulse.
- A candidate is valid only if the simulated bounding box clears all blocks and intersects
  the landing box while falling or level.

This lets us answer "is 3 block possible from standstill?", "is 4 block possible with the
available run-up?", and "can a 45-degree sprint jump reach this diagonal landing?" without
hardcoding fragile rules.

## Conservative First Implementation

1. Introduce `ParkourJumpPlanner`.
2. Replace `isValidParkourMove` with a planner call.
3. Start with only safe jumps:
   - Flat 2-block gaps from standing/walk.
   - Flat 3-block gaps with sprint and enough approach.
   - Flat 4-block gaps only with enough straight approach and strong margin.
   - Lower landings when safe to fall.
4. Reject:
   - Upward 4-block jumps.
   - Neo/corner jumps.
   - Headhitter momentum.
   - Ice/slime-special parkour.
5. Add debug reasons so rejected jumps explain why: no headroom, no approach, no landing
   support, arc collision, too little margin.

## Later Expansion

- Add `MomentumSetupMovement` before hard 4-block jumps.
- Add headhitter timing as a separate strategy.
- Add neo movements with lateral clearance checks.
- Add speed/slowness/jump-boost modifiers.
- Add block-specific friction: ice, slime, honey, soul sand.
- Record failed parkour attempts and temporarily blacklist the jump edge.
