package fr.riege.ebsl.general.task.processor;

import fr.riege.ebsl.pathfinding.rotation.AngleUtils;
import fr.riege.ebsl.pathfinding.rotation.Rotation;
import fr.riege.ebsl.pathfinding.rotation.RotationExecutor;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class AimProcessor {
    private static final float DEFAULT_YAW_SPEED = 120.0f;
    private static final float DEFAULT_PITCH_SPEED = 85.0f;
    private static final float DEFAULT_FILTER_SECONDS = 0.18f;
    private static final float DEADZONE_DEGREES = 0.18f;
    private static final float FIRST_FRAME_SECONDS = 1.0f / 60.0f;
    private static final float MAX_FRAME_SECONDS = 0.05f;

    private final float yawSpeed;
    private final float pitchSpeed;
    private final float filterSeconds;
    private float filteredYaw = Float.NaN;
    private float filteredPitch = Float.NaN;
    private long lastUpdateNanos;

    public AimProcessor() {
        this(DEFAULT_YAW_SPEED, DEFAULT_PITCH_SPEED, DEFAULT_FILTER_SECONDS);
    }

    public AimProcessor(float yawSpeed, float pitchSpeed) {
        this(yawSpeed, pitchSpeed, DEFAULT_FILTER_SECONDS);
    }

    public AimProcessor(float yawSpeed, float pitchSpeed, float filterSeconds) {
        this.yawSpeed = Math.max(1.0f, yawSpeed);
        this.pitchSpeed = Math.max(1.0f, pitchSpeed);
        this.filterSeconds = Mth.clamp(filterSeconds, 0.03f, 1.0f);
    }

    public void aimAt(Minecraft mc, Vec3 target) {
        if (mc.player == null || target == null) {
            return;
        }

        RotationExecutor.stopRotating();
        float dt = frameSeconds();
        Rotation targetRotation = AngleUtils.getRotation(mc.player.getEyePosition(), target);
        float previousYaw = mc.player.getYRot();
        float previousPitch = mc.player.getXRot();
        updateFilteredTarget(previousYaw, previousPitch, targetRotation, dt);

        float yawDelta = AngleUtils.getRotationDelta(previousYaw, filteredYaw);
        float pitchDelta = Mth.clamp(filteredPitch - previousPitch, -180.0f, 180.0f);
        if (Math.abs(yawDelta) < DEADZONE_DEGREES && Math.abs(pitchDelta) < DEADZONE_DEGREES) {
            return;
        }

        float yawStep = Mth.clamp(yawDelta, -yawSpeed * dt, yawSpeed * dt);
        float pitchStep = Mth.clamp(pitchDelta, -pitchSpeed * dt, pitchSpeed * dt);
        float yaw = applyGcdStep(mc, previousYaw, yawStep);
        float pitch = applyGcdStep(mc, previousPitch, pitchStep);

        mc.player.setYRot(yaw);
        mc.player.setXRot(Mth.clamp(pitch, -90.0f, 90.0f));
        mc.player.yRotO = previousYaw;
        mc.player.xRotO = previousPitch;
        mc.player.yHeadRotO = previousYaw;
        mc.player.yBodyRotO = previousYaw;
        mc.player.yHeadRot = yaw;
        mc.player.yBodyRot = yaw;
    }

    public void reset() {
        filteredYaw = Float.NaN;
        filteredPitch = Float.NaN;
        lastUpdateNanos = 0L;
    }

    private float frameSeconds() {
        long now = System.nanoTime();
        if (lastUpdateNanos == 0L) {
            lastUpdateNanos = now;
            return FIRST_FRAME_SECONDS;
        }
        float dt = (now - lastUpdateNanos) / 1_000_000_000.0f;
        lastUpdateNanos = now;
        return Mth.clamp(dt, 0.001f, MAX_FRAME_SECONDS);
    }

    private void updateFilteredTarget(float currentYaw, float currentPitch, Rotation targetRotation, float dt) {
        if (Float.isNaN(filteredYaw) || Float.isNaN(filteredPitch)) {
            filteredYaw = currentYaw;
            filteredPitch = currentPitch;
        }

        float smoothing = 1.0f - (float) Math.exp(-dt / filterSeconds);
        float targetYawDelta = AngleUtils.getRotationDelta(filteredYaw, targetRotation.yaw);
        filteredYaw += targetYawDelta * smoothing;
        filteredPitch += (Mth.clamp(targetRotation.pitch, -90.0f, 90.0f) - filteredPitch) * smoothing;
        filteredPitch = Mth.clamp(filteredPitch, -90.0f, 90.0f);
    }

    private static float applyGcdStep(Minecraft mc, float previous, float step) {
        double gcd = computeGcd(mc);
        double roundedStep = Math.round(step / gcd) * gcd;
        if (Math.abs(roundedStep) < gcd) {
            return previous;
        }
        return (float) (previous + roundedStep);
    }

    private static double computeGcd(Minecraft mc) {
        double f = mc.options.sensitivity().get() * 0.6 + 0.2;
        return f * f * f * 1.2;
    }
}
