package fr.riege.ebsl.common.pathfinding.execution;

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;

final class CameraTargetSmoother {
    private boolean initialized;
    private double y;
    private double velocityY;

    void reset(double initialY) {
        initialized = true;
        y = initialY;
        velocityY = 0.0;
    }

    void clear() {
        initialized = false;
        y = 0.0;
        velocityY = 0.0;
    }

    Vec3d smooth(Vec3d target, Vec3d eye) {
        if (target == null) {
            return null;
        }
        if (!initialized) {
            reset(target.y());
            return target;
        }

        PathfinderSettings settings = PathfinderSettings.instance();
        double deadband = settings.cameraHeightDeadband.value();
        double stiffness = settings.cameraHeightStiffness.value();
        double damping = settings.cameraHeightDamping.value();
        double maxStep = settings.cameraHeightMaxStep.value();
        double minY = eye.y() - settings.cameraHeightDownLimit.value();
        double maxY = eye.y() + settings.cameraHeightUpLimit.value();

        double desiredY = Math.clamp(target.y(), minY, maxY);
        double error = desiredY - y;
        if (Math.abs(error) <= deadband) {
            velocityY *= damping * 0.5;
        } else {
            velocityY = velocityY * damping + (Math.abs(error) - deadband) * Math.signum(error) * stiffness;
        }

        velocityY = Math.clamp(velocityY, -maxStep, maxStep);
        y = Math.clamp(y + velocityY, minY, maxY);
        return new Vec3d(target.x(), y, target.z());
    }

    double y() {
        return y;
    }
}
