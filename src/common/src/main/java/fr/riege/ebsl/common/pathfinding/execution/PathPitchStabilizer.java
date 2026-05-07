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
        float stiffness = (float) (double) PathfinderSettings.instance().pitchSpringStiffness.value();
        float damping = (float) (double) PathfinderSettings.instance().pitchSpringDamping.value();
        velocity = velocity * damping + error * stiffness;
        // Prevent overshoot: velocity must not push stablePitch past clampedCandidate
        velocity = Math.clamp(velocity, Math.min(error, 0f), Math.max(error, 0f));
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
