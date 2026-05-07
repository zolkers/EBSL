# Pitch Spring-Damper — Design Spec

**Date:** 2026-05-07  
**Goal:** Replace the step-based `PathPitchStabilizer` with a spring-damper that runs every tick, decoupled from the yaw dispatch system, for human-like pitch movement.

---

## Problem

The current pitch system uses `PathPitchStabilizer` with deadband + `maxStep` clamping + `pitchSnapToNeutralDeg`. This produces mechanical, robotic pitch: the camera only moves its Y axis at node height transitions, and snaps back to 0 immediately. The yaw (X) already feels natural via timed easing; the pitch needs an equivalent organic system.

---

## Architecture

**Before:**
```
updateRotation() → PitchStabilizer (deadband/maxStep) → desiredRot(yaw+pitch) → dispatch → RotationExecutor animates yaw+pitch
```

**After:**
```
updateRotation() → PitchSpring.tick()  → stablePitch stored in stabilizer
               → dispatch yaw only  → RotationExecutor animates yaw
tickExecutor() → RotationExecutor.update(stablePitch) → physics.setRotation(animatedYaw, springPitch)
```

Pitch is updated every tick via spring physics. Yaw keeps its existing dispatch + timed easing system unchanged.

---

## `PathPitchStabilizer` — Spring-Damper

**Removed:** `deadband`, `maxStep`, `pitchSnapToNeutralDeg` behavior (replaced by spring dynamics).

**State:**
```java
private float stablePitch;
private float velocity;      // new
```

**New `tick()` method** (called every navigation tick):
```java
float tick(float candidate, boolean inWater, float maxAbsPitch) {
    float error = AngleUtils.getRotationDelta(stablePitch, candidate);
    velocity = velocity * damping + error * stiffness;
    stablePitch = Math.clamp(stablePitch + velocity, -maxAbsPitch, maxAbsPitch);
    return stablePitch;
}

float getStablePitch() { return stablePitch; }
```

**`reset()`:** sets `velocity = 0`, `stablePitch` = clamped current player pitch.

**`pitchMinHorizontalDistance` preserved:** spring is not fed when horizontal distance to target is below threshold (avoids pitch flips when standing on the target node).

---

## `RotationExecutor` — Pitch Override

New overload that accepts a pitch override from the spring instead of animating pitch:

```java
public void update(float pitchOverride) {
    float clamped = Math.max(-90f, Math.min(90f, applyGcd(pitchOverride, player.pitch(), -90f, 90f)));
    if (!rotating || currentStrategy == null) {
        physics.setRotation(player.yaw(), clamped);
        return;
    }
    Rotation result = currentStrategy.onRotate(player, targetYaw, targetPitch);
    if (result == null) {
        stopRotating();
        physics.setRotation(player.yaw(), clamped);
        return;
    }
    float newYaw = applyGcd(result.yaw, player.yaw());
    physics.setRotation(newYaw, clamped);
}
```

Original `update()` is kept for backward compatibility.

---

## `PathRotationController` — Wiring

**`updateRotation()`:**
- Call `pitchStabilizer.tick(rawRot.pitch, inWater, maxAbsPitch)` every tick to advance spring state.
- Dispatch decision uses **yaw drift only** — `pitchDrift > pitchThreshold` is removed as a dispatch trigger.
- `rotateTo()` is called with `new Rotation(rawRot.yaw, 0f)` (pitch value irrelevant, spring owns it).

**`tickExecutor()`:**
```java
void tickExecutor() {
    rotationExecutor.update(pitchStabilizer.getStablePitch());
}
```

**`dispatchRotationIfNeeded()`:** remove `pitchDrift` check from dispatch condition.

**`rebuild()` / `reset()`:** call `pitchStabilizer.reset()` as before.

---

## `PathfinderSettings` — New Parameters

| Key | Default | Description |
|---|---|---|
| `pitchSpringStiffness` | `0.10` | Pull force toward target pitch per tick |
| `pitchSpringDamping` | `0.75` | Velocity decay per tick (< 1 = damped, ~0.72–0.78 = slightly underdamped → micro-overshoot) |

Existing `pitchLandMaxAbsDeg` / `pitchWaterMaxAbsDeg` and `pitchMinHorizontalDistance` are preserved.

Existing `pitchLandDeadbandDeg`, `pitchWaterDeadbandDeg`, `pitchLandMaxStepDeg`, `pitchWaterMaxStepDeg`, `pitchSnapToNeutralDeg` become unused and can be deprecated/removed.

---

## Files Changed

| File | Change |
|---|---|
| `PathPitchStabilizer.java` | Full rewrite — spring-damper, `tick()` + `getStablePitch()` |
| `RotationExecutor.java` | Add `update(float pitchOverride)` overload |
| `PathRotationController.java` | Wire spring tick, dispatch yaw-only, `tickExecutor` uses pitch override |
| `PathfinderSettings` (settings file) | Add `pitchSpringStiffness`, `pitchSpringDamping` |
