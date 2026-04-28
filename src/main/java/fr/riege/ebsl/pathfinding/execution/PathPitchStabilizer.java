package fr.riege.ebsl.pathfinding.execution;

import fr.riege.ebsl.pathfinding.PathfinderConfig;
import fr.riege.ebsl.pathfinding.rotation.AngleUtils;
import fr.riege.ebsl.pathfinding.rotation.Rotation;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

final class PathPitchStabilizer {
    private static final double MIN_HORIZONTAL_DISTANCE = 2.25;
    private static final float LAND_DEADBAND_DEG = 3.0f;
    private static final float WATER_DEADBAND_DEG = 10.0f;
    private static final float LAND_MAX_ABS_PITCH_DEG = 22.0f;
    private static final float WATER_MAX_ABS_PITCH_DEG = 8.0f;
    private static final float MAX_ACCEPTED_STEP_DEG = 7.0f;
    private static final float WATER_MAX_ACCEPTED_STEP_DEG = 3.0f;
    private static final float SNAP_TO_NEUTRAL_DEG = 1.0f;

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

        float maxAbsPitch = inWater ? WATER_MAX_ABS_PITCH_DEG : LAND_MAX_ABS_PITCH_DEG;
        if (!initialized) {
            stablePitch = clamp(mc.player.getXRot(), -maxAbsPitch, maxAbsPitch);
            initialized = true;
        }

        float deadband = inWater ? WATER_DEADBAND_DEG : LAND_DEADBAND_DEG;
        float maxStep = inWater ? WATER_MAX_ACCEPTED_STEP_DEG : MAX_ACCEPTED_STEP_DEG;
        float candidate = clamp(rawRotation.pitch, -maxAbsPitch, maxAbsPitch);
        float deltaFromStable = AngleUtils.getRotationDelta(stablePitch, candidate);

        if (horizontalDistance < MIN_HORIZONTAL_DISTANCE || Math.abs(deltaFromStable) < deadband) {
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
        if (!inWater && Math.abs(stablePitch) <= SNAP_TO_NEUTRAL_DEG) {
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
