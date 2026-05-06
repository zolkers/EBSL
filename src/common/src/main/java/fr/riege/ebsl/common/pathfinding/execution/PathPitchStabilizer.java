package fr.riege.ebsl.common.pathfinding.execution;

import fr.riege.ebsl.common.layer.IPlayerLayer;
import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.pathfinding.rotation.AngleUtils;
import fr.riege.ebsl.common.pathfinding.rotation.Rotation;
import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;

import java.util.function.Consumer;

final class PathPitchStabilizer {
    private boolean initialized;
    private float stablePitch;

    void reset() {
        initialized = false;
        stablePitch = 0.0f;
    }

    Rotation stabilize(IPlayerLayer player, Vec3d target, Rotation rawRotation, Consumer<String> debug) {
        Vec3d eye = player.eyePosition();
        double dx = target.x() - eye.x();
        double dz = target.z() - eye.z();
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
        boolean inWater = player.isInWater();

        float maxAbsPitch = (float) (double) (inWater
            ? PathfinderSettings.instance().pitchWaterMaxAbsDeg.value()
            : PathfinderSettings.instance().pitchLandMaxAbsDeg.value());
        if (!initialized) {
            stablePitch = clamp(player.pitch(), -maxAbsPitch, maxAbsPitch);
            initialized = true;
        }

        float deadband = (float) (double) (inWater
            ? PathfinderSettings.instance().pitchWaterDeadbandDeg.value()
            : PathfinderSettings.instance().pitchLandDeadbandDeg.value());
        float maxStep = (float) (double) (inWater
            ? PathfinderSettings.instance().pitchWaterMaxStepDeg.value()
            : PathfinderSettings.instance().pitchLandMaxStepDeg.value());
        float candidate = clamp(rawRotation.pitch, -maxAbsPitch, maxAbsPitch);
        float deltaFromStable = AngleUtils.getRotationDelta(stablePitch, candidate);

        if (horizontalDistance < PathfinderSettings.instance().pitchMinHorizontalDistance.value()
            || Math.abs(deltaFromStable) < deadband) {
            float held = holdPitch(inWater, maxAbsPitch);
            debug(debug, "pitch stable hold raw=%.2f candidate=%.2f held=%.2f delta=%.2f horiz=%.2f water=%s",
                rawRotation.pitch, candidate, held, deltaFromStable, horizontalDistance, inWater);
            return new Rotation(rawRotation.yaw, held);
        }

        float accepted = stablePitch + clamp(deltaFromStable, -maxStep, maxStep);
        stablePitch = clamp(accepted, -maxAbsPitch, maxAbsPitch);
        debug(debug, "pitch stable accept raw=%.2f candidate=%.2f stable=%.2f delta=%.2f horiz=%.2f water=%s",
            rawRotation.pitch, candidate, stablePitch, deltaFromStable, horizontalDistance, inWater);
        return new Rotation(rawRotation.yaw, stablePitch);
    }

    private float holdPitch(boolean inWater, float maxAbsPitch) {
        stablePitch = clamp(stablePitch, -maxAbsPitch, maxAbsPitch);
        if (!inWater && Math.abs(stablePitch) <= PathfinderSettings.instance().pitchSnapToNeutralDeg.value()) {
            stablePitch = 0.0f;
        }
        return stablePitch;
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static void debug(Consumer<String> debug, String message, Object... args) {
        if (debug != null && PathfinderSettings.instance().showDebug.value()) {
            debug.accept(String.format(message, args));
        }
    }
}
