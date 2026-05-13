/*
 * This file is part of EBSL.
 *
 * EBSL is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EBSL is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package fr.riege.ebsl.common.pathfinding.rotation;

import fr.riege.ebsl.common.world.layer.IPlayerLayer;

public final class TimedEaseStrategy implements IRotationStrategy {
    private final EasingType yawEasing;
    private final EasingType pitchEasing;
    private final long duration;

    private float startYaw;
    private float startPitch;
    private long endTime;

    public TimedEaseStrategy(EasingType yawEasing, EasingType pitchEasing, long durationMs) {
        this.yawEasing = yawEasing;
        this.pitchEasing = pitchEasing;
        this.duration = durationMs;
    }

    public TimedEaseStrategy(EasingType easing, long durationMs) {
        this(easing, easing, durationMs);
    }

    @Override
    public void onStart(IPlayerLayer player) {
        startYaw = player.yaw();
        startPitch = player.pitch();
        endTime = System.currentTimeMillis() + duration;
    }

    @Override
    public Rotation onRotate(IPlayerLayer player, float targetYaw, float targetPitch) {
        long now = System.currentTimeMillis();
        if (now >= endTime) {
            return new Rotation(targetYaw, targetPitch);
        }

        float progress = 1f - ((float) (endTime - now) / (float) duration);
        float t = Math.clamp(progress, 0f, 1f);
        float yawDelta = AngleUtils.normalizeAngle(targetYaw - startYaw);
        float yaw = yawEasing.apply(startYaw, startYaw + yawDelta, t);
        float pitch = clampPitch(pitchEasing.apply(startPitch, clampPitch(targetPitch), t));
        return new Rotation(yaw, pitch);
    }

    private static float clampPitch(float pitch) {
        return Math.clamp(pitch, -90f, 90f);
    }
}
