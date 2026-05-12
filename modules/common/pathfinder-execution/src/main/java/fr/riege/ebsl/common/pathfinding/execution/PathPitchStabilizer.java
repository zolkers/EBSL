package fr.riege.ebsl.common.pathfinding.execution;

import fr.riege.ebsl.common.world.layer.IPlayerLayer;
import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.pathfinding.rotation.AngleUtils;
import fr.riege.ebsl.common.pathfinding.rotation.Rotation;
import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;

import java.util.function.Consumer;

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

    
    Rotation stabilize(IPlayerLayer player, Vec3d target, Rotation rawRot, Consumer<String> debug) {
        float stabilizedPitch = tick(rawRot.pitch, player.isInWater());
        return new Rotation(rawRot.yaw, stabilizedPitch);
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
