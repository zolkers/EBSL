package fr.riege.ebsl.pathfinding.execution;

import fr.riege.ebsl.pathfinding.PathfinderConfig;
import fr.riege.ebsl.pathfinding.rotation.AngleUtils;
import fr.riege.ebsl.pathfinding.rotation.Rotation;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

final class PathPitchStabilizer {
    private boolean initialized;
    private float stablePitch;

    void reset() {
        initialized = false;
        stablePitch = 0.0f;
    }

    Rotation stabilize(Minecraft mc, Vec3 target, Rotation rawRotation, Consumer<String> debug) {
        if (mc.player == null) {
            return rawRotation;
        }

        Vec3 eye = mc.player.getEyePosition();
        double dx = target.x - eye.x;
        double dz = target.z - eye.z;
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
        boolean inWater = mc.player.isInWater();

        float maxAbsPitch = (float) (double) (inWater
            ? PathfinderConfig.PITCH_WATER_MAX_ABS_DEG.get()
            : PathfinderConfig.PITCH_LAND_MAX_ABS_DEG.get());
        if (!initialized) {
            stablePitch = clamp(mc.player.getXRot(), -maxAbsPitch, maxAbsPitch);
            initialized = true;
        }

        float deadband = (float) (double) (inWater
            ? PathfinderConfig.PITCH_WATER_DEADBAND_DEG.get()
            : PathfinderConfig.PITCH_LAND_DEADBAND_DEG.get());
        float maxStep = (float) (double) (inWater
            ? PathfinderConfig.PITCH_WATER_MAX_STEP_DEG.get()
            : PathfinderConfig.PITCH_LAND_MAX_STEP_DEG.get());
        float candidate = clamp(rawRotation.pitch, -maxAbsPitch, maxAbsPitch);
        float deltaFromStable = AngleUtils.getRotationDelta(stablePitch, candidate);

        if (horizontalDistance < PathfinderConfig.PITCH_MIN_HORIZONTAL_DISTANCE.get() || Math.abs(deltaFromStable) < deadband) {
            float held = holdPitch(inWater, maxAbsPitch);
            debug(debug,
                "pitch stable hold raw=%.2f candidate=%.2f held=%.2f delta=%.2f horiz=%.2f water=%s",
                rawRotation.pitch,
                candidate,
                held,
                deltaFromStable,
                horizontalDistance,
                inWater);
            return new Rotation(rawRotation.yaw, held);
        }

        float accepted = stablePitch + clamp(deltaFromStable, -maxStep, maxStep);
        stablePitch = clamp(accepted, -maxAbsPitch, maxAbsPitch);
        debug(debug,
            "pitch stable accept raw=%.2f candidate=%.2f stable=%.2f delta=%.2f horiz=%.2f water=%s",
            rawRotation.pitch,
            candidate,
            stablePitch,
            deltaFromStable,
            horizontalDistance,
            inWater);
        return new Rotation(rawRotation.yaw, stablePitch);
    }

    private float holdPitch(boolean inWater, float maxAbsPitch) {
        stablePitch = clamp(stablePitch, -maxAbsPitch, maxAbsPitch);
        if (inWater) {
            return stablePitch;
        }
        if (!inWater && Math.abs(stablePitch) <= PathfinderConfig.PITCH_SNAP_TO_NEUTRAL_DEG.get()) {
            stablePitch = 0.0f;
        }
        return stablePitch;
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static void debug(Consumer<String> debug, String message, Object... args) {
        if (debug != null && PathfinderConfig.SHOW_DEBUG.get()) {
            debug.accept(String.format(message, args));
        }
    }
}
