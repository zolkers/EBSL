package fr.riege.ebsl.common.pathfinding.rotation;

import fr.riege.ebsl.common.layer.IPhysicsLayer;
import fr.riege.ebsl.common.layer.IPlayerLayer;

public final class RotationExecutor {
    private final IPlayerLayer player;
    private final IPhysicsLayer physics;

    private float targetYaw;
    private float targetPitch;
    private IRotationStrategy currentStrategy;
    private boolean rotating;
    private double cachedGcd = Double.NaN;

    public RotationExecutor(IPlayerLayer player, IPhysicsLayer physics) {
        this.player = player;
        this.physics = physics;
    }

    public void rotateTo(Rotation endRot, IRotationStrategy strategy) {
        stopRotating();
        targetYaw = endRot.yaw;
        targetPitch = endRot.pitch;
        currentStrategy = strategy;
        cachedGcd = physics.rotationGcd();
        strategy.onStart(player);
        rotating = true;
    }

    public void stopRotating() {
        if (currentStrategy != null) {
            currentStrategy.onStop();
        }
        currentStrategy = null;
        rotating = false;
    }

    public boolean isRotating() {
        return rotating;
    }

    public float getTargetYaw() {
        return targetYaw;
    }

    public float getTargetPitch() {
        return targetPitch;
    }

    public void update() {
        if (!rotating || currentStrategy == null) {
            return;
        }

        Rotation result = currentStrategy.onRotate(player, targetYaw, targetPitch);
        if (result == null) {
            stopRotating();
            return;
        }

        float newYaw = applyGcd(result.yaw, player.yaw());
        float newPitch = Math.max(-90f, Math.min(90f, applyGcd(result.pitch, player.pitch(), -90f, 90f)));
        physics.setRotation(newYaw, newPitch);
    }

    public void update(float pitchOverride) {
        float clampedPitch = Math.max(-90f, Math.min(90f, applyGcd(pitchOverride, player.pitch(), -90f, 90f)));
        if (!rotating || currentStrategy == null) {
            physics.setRotation(player.yaw(), clampedPitch);
            return;
        }
        Rotation result = currentStrategy.onRotate(player, targetYaw, targetPitch);
        if (result == null) {
            stopRotating();
            physics.setRotation(player.yaw(), clampedPitch);
            return;
        }
        float newYaw = applyGcd(result.yaw, player.yaw());
        physics.setRotation(newYaw, clampedPitch);
    }

    private float applyGcd(float rotation, float prevRotation) {
        return applyGcd(rotation, prevRotation, null, null);
    }

    private float applyGcd(float rotation, float prevRotation, Float min, Float max) {
        double gcd = Double.isNaN(cachedGcd) ? physics.rotationGcd() : cachedGcd;
        double delta = AngleUtils.getRotationDelta(prevRotation, rotation);
        double roundedDelta = Math.round(delta / gcd) * gcd;
        float result = (float) (prevRotation + roundedDelta);
        if (max != null && result > max) result -= (float) gcd;
        if (min != null && result < min) result += (float) gcd;
        return result;
    }
}
