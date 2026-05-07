package fr.riege.ebsl.common.pathfinding.execution;

import fr.riege.ebsl.common.layer.IPlayerLayer;
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
        // use land limit conservatively — spring enforces water limit on next tick
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
        stablePitch = Math.clamp(stablePitch + velocity, -maxAbs, maxAbs);
        return stablePitch;
    }

    float getStablePitch() {
        return stablePitch;
    }

    // TODO: remove when PathRotationController no longer calls stabilize()
    Rotation stabilize(IPlayerLayer player, Vec3d target, Rotation rawRot, Consumer<String> debug) {
        float stabilizedPitch = tick(rawRot.pitch, player.isInWater());
        return new Rotation(rawRot.yaw, stabilizedPitch);
    }

    private static float maxAbsPitch(boolean inWater) {
        return (float) (double) (inWater
            ? PathfinderSettings.instance().pitchWaterMaxAbsDeg.value()
            : PathfinderSettings.instance().pitchLandMaxAbsDeg.value());
    }
}
