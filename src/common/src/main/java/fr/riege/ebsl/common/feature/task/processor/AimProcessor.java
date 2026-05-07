package fr.riege.ebsl.common.feature.task.processor;

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.pathfinding.rotation.AngleUtils;
import fr.riege.ebsl.common.pathfinding.rotation.Rotation;
import fr.riege.ebsl.common.platform.EbslPlatform;

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

    public AimProcessor(float yawSpeed, float pitchSpeed, float filterSeconds) {
        this.yawSpeed = Math.max(1.0f, yawSpeed);
        this.pitchSpeed = Math.max(1.0f, pitchSpeed);
        this.filterSeconds = Math.clamp(filterSeconds, 0.03f, 1.0f);
    }

    public void aimAt(EbslPlatform platform, Vec3d target) {
        if (target == null || !platform.player().isAlive()) {
            return;
        }

        float dt = frameSeconds();
        Rotation targetRotation = AngleUtils.getRotation(platform.player().eyePosition(), target);
        float previousYaw = platform.player().yaw();
        float previousPitch = platform.player().pitch();
        updateFilteredTarget(previousYaw, previousPitch, targetRotation, dt);

        float yawDelta = AngleUtils.getRotationDelta(previousYaw, filteredYaw);
        float pitchDelta = Math.clamp(filteredPitch - previousPitch, -180.0f, 180.0f);
        if (Math.abs(yawDelta) < DEADZONE_DEGREES && Math.abs(pitchDelta) < DEADZONE_DEGREES) {
            return;
        }

        float yawStep = Math.clamp(yawDelta, -yawSpeed * dt, yawSpeed * dt);
        float pitchStep = Math.clamp(pitchDelta, -pitchSpeed * dt, pitchSpeed * dt);
        float yaw = applyGcdStep(platform, previousYaw, yawStep);
        float pitch = applyGcdStep(platform, previousPitch, pitchStep);
        platform.physics().setRotation(yaw, Math.clamp(pitch, -90.0f, 90.0f));
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
        return Math.clamp(dt, 0.001f, MAX_FRAME_SECONDS);
    }

    private void updateFilteredTarget(float currentYaw, float currentPitch, Rotation targetRotation, float dt) {
        if (Float.isNaN(filteredYaw) || Float.isNaN(filteredPitch)) {
            filteredYaw = currentYaw;
            filteredPitch = currentPitch;
        }

        float smoothing = 1.0f - (float) Math.exp(-dt / filterSeconds);
        float targetYawDelta = AngleUtils.getRotationDelta(filteredYaw, targetRotation.yaw);
        filteredYaw += targetYawDelta * smoothing;
        filteredPitch += (Math.clamp(targetRotation.pitch, -90.0f, 90.0f) - filteredPitch) * smoothing;
        filteredPitch = Math.clamp(filteredPitch, -90.0f, 90.0f);
    }

    private static float applyGcdStep(EbslPlatform platform, float previous, float step) {
        double gcd = platform.physics().rotationGcd();
        double roundedStep = Math.round(step / gcd) * gcd;
        if (Math.abs(roundedStep) < gcd) {
            return previous;
        }
        return (float) (previous + roundedStep);
    }

}
