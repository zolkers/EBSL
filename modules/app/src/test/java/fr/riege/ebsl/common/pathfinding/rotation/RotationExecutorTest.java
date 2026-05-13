/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Copyright (C) 2026 Victor Riegert and EBSL contributors
 *
 * This file is part of EBSL.
 *
 * EBSL is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EBSL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EBSL. If not, see <https://www.gnu.org/licenses/>.
 */

package fr.riege.ebsl.common.pathfinding.rotation;

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.platform.layer.IPhysicsLayer;
import fr.riege.ebsl.common.world.layer.IPlayerLayer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RotationExecutorTest {



    private static IPlayerLayer fakePlayer(float yaw, float pitch) {
        return new IPlayerLayer() {
            @Override public Vec3d position() { return new Vec3d(0, 0, 0); }
            @Override public float yaw() { return yaw; }
            @Override public float pitch() { return pitch; }
            @Override public boolean isInWater() { return false; }
            @Override public boolean isInLava() { return false; }
            @Override public boolean isSprinting() { return false; }
            @Override public boolean isAlive() { return true; }
            @Override public float getHealth() { return 20f; }
        };
    }



    private static IPhysicsLayer fakePhysics(float[] capturedYaw, float[] capturedPitch) {
        return new IPhysicsLayer() {
            @Override public void setRotation(float yaw, float pitch) {
                capturedYaw[0] = yaw; capturedPitch[0] = pitch;
            }
            @Override public double rotationGcd() { return 0.1; }
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
