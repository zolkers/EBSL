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
package fr.riege.ebsl.common.pathfinding.execution;

import fr.riege.ebsl.common.pathfinding.rotation.AngleUtils;
import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;

final class PathPitchStabilizer {
    private float stablePitch;
    private float velocity;

    void reset() {
        stablePitch = 0f;
        velocity = 0f;
    }

    void reset(float initialPitch) {

        float max = maxAbsPitch(false);
        stablePitch = Math.clamp(initialPitch, -max, max);
        velocity = 0f;
    }

    float tick(float candidate, boolean inWater) {
        return tick(candidate, inWater, 0.05);
    }

    float tick(float candidate, boolean inWater, double dtSeconds) {
        float maxAbs = maxAbsPitch(inWater);
        float clampedCandidate = Math.clamp(candidate, -maxAbs, maxAbs);
        float error = AngleUtils.getRotationDelta(stablePitch, clampedCandidate);
        float stiffness = (float) (double) PathfinderSettings.instance().pitchSpringStiffness.value();
        float damping = (float) (double) PathfinderSettings.instance().pitchSpringDamping.value();
        float tickScale = (float) Math.clamp(dtSeconds * 20.0, 0.05, 2.0);
        velocity = (float) (velocity * Math.pow(damping, tickScale) + error * stiffness * tickScale);
        stablePitch = clampPitchStep(stablePitch, stablePitch + velocity * tickScale, maxAbs);
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

    private static float clampPitchStep(float current, float next, float maxAbs) {
        if (Math.abs(current) <= maxAbs) {
            return Math.clamp(next, -maxAbs, maxAbs);
        }
        if (Math.signum(next) == Math.signum(current) && Math.abs(next) > Math.abs(current)) {
            return current;
        }
        return Math.clamp(next, -90.0f, 90.0f);
    }
}
