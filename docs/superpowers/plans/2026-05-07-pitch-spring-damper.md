# Pitch Spring-Damper Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the step-based pitch stabilizer with a per-tick spring-damper decoupled from the yaw dispatch system, producing human-like organic pitch movement.

**Architecture:** `PathPitchStabilizer` becomes a spring-damper (velocity + stiffness/damping) ticked every navigation update. `RotationExecutor` gains an `update(float pitchOverride)` overload that applies the spring pitch instead of animated pitch. `PathRotationController` dispatches yaw-only and combines animated yaw with spring pitch in `tickExecutor`.

**Tech Stack:** Java 21, JUnit 5 (already configured in `src/common/build.gradle`), Gradle module `:common`.

---

## File Map

| File | Action |
|---|---|
| `src/common/src/main/java/fr/riege/ebsl/common/pathfinding/settings/PathfinderSettings.java` | Add 2 spring settings; remove 5 obsolete pitch settings in Task 4 |
| `src/common/src/main/java/fr/riege/ebsl/common/pathfinding/execution/PathPitchStabilizer.java` | Full rewrite — spring-damper |
| `src/common/src/main/java/fr/riege/ebsl/common/pathfinding/rotation/RotationExecutor.java` | Add `update(float pitchOverride)` overload |
| `src/common/src/main/java/fr/riege/ebsl/common/pathfinding/execution/PathRotationController.java` | Tick spring, dispatch yaw-only, tickExecutor uses spring pitch |
| `src/common/src/test/java/fr/riege/ebsl/common/pathfinding/execution/PathPitchStabilizerTest.java` | New — unit tests for spring |
| `src/common/src/test/java/fr/riege/ebsl/common/pathfinding/rotation/RotationExecutorTest.java` | New — unit tests for pitchOverride |

---

### Task 1: Add spring settings to PathfinderSettings

**Files:**
- Modify: `src/common/src/main/java/fr/riege/ebsl/common/pathfinding/settings/PathfinderSettings.java`

- [ ] **Step 1: Add two new fields after `pitchSnapToNeutralDeg` (line 201)**

```java
public final DoubleSetting pitchSpringStiffness = registerSetting(new DoubleSetting(
    "pitch_spring_stiffness", "Pitch spring stiffness", 0.10, 0.01, 1.0));
public final DoubleSetting pitchSpringDamping = registerSetting(new DoubleSetting(
    "pitch_spring_damping", "Pitch spring damping", 0.75, 0.01, 0.99));
```

- [ ] **Step 2: Add both to `rotationSettings()` after `pitchSnapToNeutralDeg`**

```java
INSTANCE.pitchSpringStiffness,
INSTANCE.pitchSpringDamping,
```

- [ ] **Step 3: Build to verify no compilation errors**

```bash
./gradlew :common:compileJava
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Commit**

```bash
git add src/common/src/main/java/fr/riege/ebsl/common/pathfinding/settings/PathfinderSettings.java
git commit -m "feat: add pitchSpringStiffness and pitchSpringDamping settings"
```

---

### Task 2: Rewrite PathPitchStabilizer as spring-damper

**Files:**
- Rewrite: `src/common/src/main/java/fr/riege/ebsl/common/pathfinding/execution/PathPitchStabilizer.java`
- Create: `src/common/src/test/java/fr/riege/ebsl/common/pathfinding/execution/PathPitchStabilizerTest.java`

- [ ] **Step 1: Create the test directory**

```bash
mkdir -p src/common/src/test/java/fr/riege/ebsl/common/pathfinding/execution
```

- [ ] **Step 2: Write the failing tests**

Create `src/common/src/test/java/fr/riege/ebsl/common/pathfinding/execution/PathPitchStabilizerTest.java`:

```java
package fr.riege.ebsl.common.pathfinding.execution;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PathPitchStabilizerTest {

    @Test
    void convergesFromZeroTowardPositiveCandidate() {
        var s = new PathPitchStabilizer();
        s.reset(0f);
        float prev = 0f;
        for (int i = 0; i < 40; i++) {
            float cur = s.tick(20f, false);
            assertTrue(cur >= prev, "pitch should increase each tick toward 20°, was " + prev + " then " + cur);
            prev = cur;
        }
        assertTrue(prev > 10f, "Should reach well above 10° after 40 ticks, got " + prev);
    }

    @Test
    void decaysBackToZeroWhenCandidateIsZero() {
        var s = new PathPitchStabilizer();
        s.reset(0f);
        for (int i = 0; i < 30; i++) s.tick(20f, false);
        float peak = s.getStablePitch();
        assertTrue(peak > 5f, "Should have built up some pitch first, got " + peak);

        float prev = peak;
        for (int i = 0; i < 60; i++) {
            float cur = s.tick(0f, false);
            assertTrue(cur <= prev + 0.01f, "Pitch should decrease toward 0, was " + prev + " then " + cur);
            prev = cur;
        }
        assertTrue(prev < 5f, "Should decay significantly toward 0, got " + prev);
    }

    @Test
    void clampsToLandMaxAbsPitch() {
        var s = new PathPitchStabilizer();
        s.reset(0f);
        float result = 0f;
        for (int i = 0; i < 200; i++) result = s.tick(90f, false);
        // default pitchLandMaxAbsDeg = 22.0
        assertTrue(result <= 22.0f, "Should not exceed pitchLandMaxAbsDeg=22, got " + result);
    }

    @Test
    void resetClearsVelocity() {
        var s = new PathPitchStabilizer();
        s.reset(0f);
        for (int i = 0; i < 20; i++) s.tick(30f, false);

        s.reset(5f);
        assertEquals(5f, s.getStablePitch(), 0.001f, "reset should set stablePitch to initialPitch");

        // After reset velocity=0, first tick moves only stiffness*error (default stiffness=0.10)
        float after = s.tick(15f, false);
        // error = 10, stiffness = 0.10 → velocity = 1.0, stablePitch = 6.0 max
        assertTrue(after < 7f, "Should move slowly right after reset (velocity reset to 0), got " + after);
    }

    @Test
    void waterUsesWaterMaxAbsPitch() {
        var s = new PathPitchStabilizer();
        s.reset(0f);
        float result = 0f;
        for (int i = 0; i < 200; i++) result = s.tick(90f, true);
        // default pitchWaterMaxAbsDeg = 8.0
        assertTrue(result <= 8.0f, "Should not exceed pitchWaterMaxAbsDeg=8, got " + result);
    }
}
```

- [ ] **Step 3: Run tests — verify they FAIL (class doesn't have `tick()` yet)**

```bash
./gradlew :common:test --tests "fr.riege.ebsl.common.pathfinding.execution.PathPitchStabilizerTest"
```

Expected: compile error or test failure.

- [ ] **Step 4: Rewrite PathPitchStabilizer**

Replace the entire content of `src/common/src/main/java/fr/riege/ebsl/common/pathfinding/execution/PathPitchStabilizer.java`:

```java
package fr.riege.ebsl.common.pathfinding.execution;

import fr.riege.ebsl.common.pathfinding.rotation.AngleUtils;
import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;

final class PathPitchStabilizer {
    private float stablePitch;
    private float velocity;

    void reset(float initialPitch) {
        float max = maxAbsPitch(false);
        stablePitch = Math.clamp(initialPitch, -max, max);
        velocity = 0f;
    }

    float tick(float candidate, boolean inWater) {
        float maxAbs = maxAbsPitch(inWater);
        float clampedCandidate = Math.clamp(candidate, -maxAbs, maxAbs);
        float error = AngleUtils.getRotationDelta(stablePitch, clampedCandidate);
        float stiffness = (float) PathfinderSettings.instance().pitchSpringStiffness.value();
        float damping = (float) PathfinderSettings.instance().pitchSpringDamping.value();
        velocity = velocity * damping + error * stiffness;
        stablePitch = Math.clamp(stablePitch + velocity, -maxAbs, maxAbs);
        return stablePitch;
    }

    float getStablePitch() {
        return stablePitch;
    }

    private static float maxAbsPitch(boolean inWater) {
        return (float) (double) (inWater
            ? PathfinderSettings.instance().pitchWaterMaxAbsDeg.value()
            : PathfinderSettings.instance().pitchLandMaxAbsDeg.value());
    }
}
```

- [ ] **Step 5: Run tests — verify they PASS**

```bash
./gradlew :common:test --tests "fr.riege.ebsl.common.pathfinding.execution.PathPitchStabilizerTest"
```

Expected: `BUILD SUCCESSFUL`, all 5 tests green.

- [ ] **Step 6: Commit**

```bash
git add src/common/src/main/java/fr/riege/ebsl/common/pathfinding/execution/PathPitchStabilizer.java \
        src/common/src/test/java/fr/riege/ebsl/common/pathfinding/execution/PathPitchStabilizerTest.java
git commit -m "feat: rewrite PathPitchStabilizer as spring-damper"
```

---

### Task 3: Add `update(float pitchOverride)` to RotationExecutor

**Files:**
- Modify: `src/common/src/main/java/fr/riege/ebsl/common/pathfinding/rotation/RotationExecutor.java`
- Create: `src/common/src/test/java/fr/riege/ebsl/common/pathfinding/rotation/RotationExecutorTest.java`

- [ ] **Step 1: Create the test directory**

```bash
mkdir -p src/common/src/test/java/fr/riege/ebsl/common/pathfinding/rotation
```

- [ ] **Step 2: Write the failing test**

Create `src/common/src/test/java/fr/riege/ebsl/common/pathfinding/rotation/RotationExecutorTest.java`:

```java
package fr.riege.ebsl.common.pathfinding.rotation;

import fr.riege.ebsl.common.layer.IPhysicsLayer;
import fr.riege.ebsl.common.layer.IPlayerLayer;
import fr.riege.ebsl.common.math.Vec3d;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RotationExecutorTest {

    // IPlayerLayer: only position(), isInWater(), isInLava(), isSprinting(), isAlive(), getHealth() are non-default.
    // yaw() and pitch() have defaults (0f) — override them with the desired values.
    private static IPlayerLayer fakePlayer(float yaw, float pitch) {
        return new IPlayerLayer() {
            @Override public Vec3d position() { return new Vec3d(0, 0, 0); }
            @Override public float yaw()      { return yaw; }
            @Override public float pitch()    { return pitch; }
            @Override public boolean isInWater()  { return false; }
            @Override public boolean isInLava()   { return false; }
            @Override public boolean isSprinting() { return false; }
            @Override public boolean isAlive()    { return true; }
            @Override public float getHealth()    { return 20f; }
        };
    }

    // IPhysicsLayer: only setSneak() and clearInputs() are non-default.
    // setRotation() and rotationGcd() have defaults — override what the test needs.
    private static IPhysicsLayer fakePhysics(float[] capturedYaw, float[] capturedPitch) {
        return new IPhysicsLayer() {
            @Override public void setRotation(float yaw, float pitch) {
                capturedYaw[0] = yaw; capturedPitch[0] = pitch;
            }
            @Override public double rotationGcd() { return 0.1; }
            @Override public void setSneak(boolean v) {}
            @Override public void clearInputs() {}
        };
    }

    @Test
    void updateWithPitchOverrideAppliesSpringPitchWhenNotRotating() {
        float[] capturedYaw = {0f};
        float[] capturedPitch = {0f};

        var exec = new RotationExecutor(fakePlayer(30f, 0f), fakePhysics(capturedYaw, capturedPitch));
        exec.update(15f);

        assertEquals(30f, capturedYaw[0], 1f, "yaw should match player yaw when not rotating");
        assertEquals(15f, capturedPitch[0], 1f, "pitch should match spring override");
    }

    @Test
    void updateWithPitchOverrideClampsTo90() {
        float[] capturedYaw = {0f};
        float[] capturedPitch = {0f};

        var exec = new RotationExecutor(fakePlayer(0f, 0f), fakePhysics(capturedYaw, capturedPitch));
        exec.update(120f);
        assertTrue(capturedPitch[0] <= 90f, "pitch should be clamped to 90, got " + capturedPitch[0]);
    }

    @Test
    void updateWithPitchOverrideUsesAnimatedYawWhenRotating() {
        float[] capturedYaw = {0f};
        float[] capturedPitch = {0f};

        var exec = new RotationExecutor(fakePlayer(0f, 0f), fakePhysics(capturedYaw, capturedPitch));
        exec.rotateTo(new Rotation(90f, 0f),
            new TimedEaseStrategy(EasingType.LINEAR, 10_000L));
        exec.update(20f);

        assertTrue(capturedYaw[0] >= 0f && capturedYaw[0] <= 90f,
            "yaw should be animating toward 90°, got " + capturedYaw[0]);
        assertEquals(20f, capturedPitch[0], 1f, "pitch must be spring override, not animated pitch, got " + capturedPitch[0]);
    }
}
```

- [ ] **Step 3: Run tests — verify FAIL**

```bash
./gradlew :common:test --tests "fr.riege.ebsl.common.pathfinding.rotation.RotationExecutorTest"
```

Expected: compile error — `update(float)` doesn't exist yet.

- [ ] **Step 4: Add `update(float pitchOverride)` to RotationExecutor**

In `src/common/src/main/java/fr/riege/ebsl/common/pathfinding/rotation/RotationExecutor.java`, add after the existing `update()` method:

```java
public void update(float pitchOverride) {
    float clampedPitch = Math.max(-90f, Math.min(90f, applyGcd(pitchOverride, player.pitch(), -90f, 90f)));
    if (!rotating || currentStrategy == null) {
        physics.setRotation(player.yaw(), clampedPitch);
        return;
    }
    Rotation result = currentStrategy.onRotate(player, targetYaw, targetPitch);
    if (result == null) {
        stopRotating();
        physics.setRotation(player.yaw(), clampedPitch);
        return;
    }
    float newYaw = applyGcd(result.yaw, player.yaw());
    physics.setRotation(newYaw, clampedPitch);
}
```

- [ ] **Step 5: Run tests — verify PASS**

```bash
./gradlew :common:test --tests "fr.riege.ebsl.common.pathfinding.rotation.RotationExecutorTest"
```

Expected: `BUILD SUCCESSFUL`, all 3 tests green.

- [ ] **Step 6: Commit**

```bash
git add src/common/src/main/java/fr/riege/ebsl/common/pathfinding/rotation/RotationExecutor.java \
        src/common/src/test/java/fr/riege/ebsl/common/pathfinding/rotation/RotationExecutorTest.java
git commit -m "feat: add RotationExecutor.update(pitchOverride) for spring-driven pitch"
```

---

### Task 4: Wire PathRotationController — spring tick + yaw-only dispatch

**Files:**
- Modify: `src/common/src/main/java/fr/riege/ebsl/common/pathfinding/execution/PathRotationController.java`

- [ ] **Step 1: Update `rebuild()` — pass initial pitch to spring reset**

In `rebuild()` (line 46), change:
```java
this.pitchStabilizer.reset();
```
to:
```java
this.pitchStabilizer.reset(player.pitch());
```

- [ ] **Step 2: Update `reset()` — pass initial pitch to spring reset**

In `reset()` (line 56), change:
```java
this.pitchStabilizer.reset();
```
to:
```java
this.pitchStabilizer.reset(player.pitch());
```

- [ ] **Step 3: Update `updateRotation()` — tick spring, pass yaw-only to dispatch**

Replace the last 3 lines of `updateRotation()` (lines 85–87):
```java
Rotation rawRot = AngleUtils.getRotation(player.eyePosition(), rotationTarget.position());
Rotation desiredRot = pitchStabilizer.stabilize(player, rotationTarget.position(), rawRot, debug);
dispatchRotationIfNeeded(pursuitSegment, debug, desiredRot, rotationTarget);
```
with:
```java
Rotation rawRot = AngleUtils.getRotation(player.eyePosition(), rotationTarget.position());

Vec3d eye = player.eyePosition();
double dx = rotationTarget.position().x() - eye.x();
double dz = rotationTarget.position().z() - eye.z();
double horizDist = Math.sqrt(dx * dx + dz * dz);
float candidatePitch = horizDist >= PathfinderSettings.instance().pitchMinHorizontalDistance.value()
    ? rawRot.pitch : 0f;
pitchStabilizer.tick(candidatePitch, player.isInWater());
debug(debug, "pitch spring stable=%.2f candidate=%.2f horizDist=%.2f",
    pitchStabilizer.getStablePitch(), candidatePitch, horizDist);

dispatchYawIfNeeded(pursuitSegment, debug, rawRot.yaw, rotationTarget);
```

- [ ] **Step 4: Rename and rewrite `dispatchRotationIfNeeded` → `dispatchYawIfNeeded`**

Replace the entire `dispatchRotationIfNeeded` method with:

```java
private void dispatchYawIfNeeded(int pursuitSegment, Consumer<String> debug,
                                  float desiredYaw, RotationTarget rotationTarget) {
    boolean alreadyRotating = rotationExecutor.isRotating();
    float referenceYaw = alreadyRotating ? rotationExecutor.getTargetYaw() : player.yaw();
    float yawDrift = Math.abs(AngleUtils.getRotationDelta(referenceYaw, desiredYaw));

    debug(debug, "desired yaw=%.2f reference=%.2f drift=%.2f", desiredYaw, referenceYaw, yawDrift);

    long now = System.currentTimeMillis();
    if (yawDrift > rotationTarget.yawThreshold()
        && (!alreadyRotating || now - lastRotationDispatchMs
            >= PathfinderSettings.instance().rotationRedispatchCooldownMs.value())) {
        debug(debug, "rotateTo dispatch easing=%s durationMs=%d wp=%d camTargetIdx=%d mode=%s registry=%s",
            rotationTarget.easing(), rotationTarget.durationMs(), pursuitSegment, camTargetIdx,
            rotationTarget.mode(), rotationTarget.registry());
        rotationExecutor.rotateTo(new Rotation(desiredYaw, 0f),
            new TimedEaseStrategy(rotationTarget.easing(), rotationTarget.durationMs()));
        lastRotationDispatchMs = now;
        return;
    }

    debug(debug, "rotateTo skipped small yaw drift wp=%d camTargetIdx=%d", pursuitSegment, camTargetIdx);
}
```

- [ ] **Step 5: Update `tickExecutor()` — use spring pitch**

Replace:
```java
void tickExecutor() {
    rotationExecutor.update();
}
```
with:
```java
void tickExecutor() {
    rotationExecutor.update(pitchStabilizer.getStablePitch());
}
```

- [ ] **Step 6: Build to verify no compilation errors**

```bash
./gradlew :common:compileJava
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 7: Run all tests**

```bash
./gradlew :common:test
```

Expected: `BUILD SUCCESSFUL`, all tests green.

- [ ] **Step 8: Commit**

```bash
git add src/common/src/main/java/fr/riege/ebsl/common/pathfinding/execution/PathRotationController.java
git commit -m "feat: wire pitch spring-damper into PathRotationController, dispatch yaw-only"
```

---

### Task 5: Remove obsolete pitch settings from PathfinderSettings

**Files:**
- Modify: `src/common/src/main/java/fr/riege/ebsl/common/pathfinding/settings/PathfinderSettings.java`

These 5 fields are now unused (only referenced `PathPitchStabilizer.stabilize()` which no longer exists):
- `pitchLandDeadbandDeg`
- `pitchWaterDeadbandDeg`
- `pitchLandMaxStepDeg`
- `pitchWaterMaxStepDeg`
- `pitchSnapToNeutralDeg`

- [ ] **Step 1: Delete the 5 field declarations**

Remove these lines from `PathfinderSettings.java`:
```java
public final DoubleSetting pitchLandDeadbandDeg = registerSetting(new DoubleSetting(
    "pitch_land_deadband_deg", "Pitch land deadband", 3.0, 0.0, 45.0));
public final DoubleSetting pitchWaterDeadbandDeg = registerSetting(new DoubleSetting(
    "pitch_water_deadband_deg", "Pitch water deadband", 10.0, 0.0, 60.0));
public final DoubleSetting pitchLandMaxStepDeg = registerSetting(new DoubleSetting(
    "pitch_land_max_step_deg", "Pitch land max step", 7.0, 0.0, 45.0));
public final DoubleSetting pitchWaterMaxStepDeg = registerSetting(new DoubleSetting(
    "pitch_water_max_step_deg", "Pitch water max step", 3.0, 0.0, 45.0));
public final DoubleSetting pitchSnapToNeutralDeg = registerSetting(new DoubleSetting(
    "pitch_snap_to_neutral_deg", "Pitch snap neutral", 1.0, 0.0, 15.0));
```

- [ ] **Step 2: Remove the same 5 from `rotationSettings()`**

In `rotationSettings()`, remove these 5 entries:
```java
INSTANCE.pitchLandDeadbandDeg,
INSTANCE.pitchWaterDeadbandDeg,
INSTANCE.pitchLandMaxStepDeg,
INSTANCE.pitchWaterMaxStepDeg,
INSTANCE.pitchSnapToNeutralDeg,
```

- [ ] **Step 3: Build and test**

```bash
./gradlew :common:compileJava && ./gradlew :common:test
```

Expected: `BUILD SUCCESSFUL`, all tests green.

- [ ] **Step 4: Commit**

```bash
git add src/common/src/main/java/fr/riege/ebsl/common/pathfinding/settings/PathfinderSettings.java
git commit -m "chore: remove obsolete step-based pitch settings replaced by spring-damper"
```
