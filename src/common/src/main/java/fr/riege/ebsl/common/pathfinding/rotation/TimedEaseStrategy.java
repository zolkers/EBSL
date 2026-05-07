package fr.riege.ebsl.common.pathfinding.rotation;

import fr.riege.ebsl.common.platform.layer.IPlayerLayer;

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
