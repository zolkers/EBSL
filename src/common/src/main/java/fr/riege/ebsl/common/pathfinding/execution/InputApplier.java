package fr.riege.ebsl.common.pathfinding.execution;

import fr.riege.ebsl.common.platform.layer.IPhysicsLayer;
import fr.riege.ebsl.common.platform.layer.IPlayerLayer;

final class InputApplier {
    private static final double STRAFE_SUPPRESSION_FORWARD_DOT = 0.45;
    private static final double STRAFE_SUPPRESSION_DOT = 0.55;

    private InputApplier() {
    }

    static void releaseAll(IPhysicsLayer physics, boolean keepSneaking) {
        physics.clearInputs();
        physics.setSneak(keepSneaking);
    }

    static void applyRelativeMovement(IPlayerLayer player, IPhysicsLayer physics, double dx, double dz,
                                      double forwardThreshold,
                                      double backwardThreshold,
                                      double strafeThreshold) {
        float yawRad = (float) Math.toRadians(player.yaw());
        double forwardX = -Math.sin(yawRad);
        double forwardZ = Math.cos(yawRad);
        double rightX = -Math.sin(yawRad + Math.PI / 2.0);
        double rightZ = Math.cos(yawRad + Math.PI / 2.0);

        double forwardDot = dx * forwardX + dz * forwardZ;
        double strafeDot = dx * rightX + dz * rightZ;

        physics.setForward(forwardDot > forwardThreshold);
        physics.setBackward(forwardDot < backwardThreshold);

        boolean mostlyForward = forwardDot > STRAFE_SUPPRESSION_FORWARD_DOT;
        double effectiveStrafeThreshold = mostlyForward
            ? Math.max(strafeThreshold, STRAFE_SUPPRESSION_DOT)
            : strafeThreshold;
        physics.setRight(strafeDot > effectiveStrafeThreshold);
        physics.setLeft(strafeDot < -effectiveStrafeThreshold);
    }

    static void applyGoalCentering(IPlayerLayer player, IPhysicsLayer physics, double dx, double dz) {
        float yawToGoal = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float relativeYaw = wrapDegrees(yawToGoal - player.yaw());

        physics.setForward(relativeYaw > -67.5 && relativeYaw <= 67.5);
        physics.setBackward(relativeYaw > 112.5 || relativeYaw <= -112.5);
        physics.setLeft(relativeYaw > -157.5 && relativeYaw <= -22.5);
        physics.setRight(relativeYaw > 22.5 && relativeYaw <= 157.5);
        physics.setSprint(false);
        boolean inWater = player.isInWater();
        physics.setJump(inWater);
        physics.setSneak(!inWater);
    }

    static void applyCornerAlignment(IPlayerLayer player, IPhysicsLayer physics, double dx, double dz) {
        double hDist = Math.sqrt(dx * dx + dz * dz);
        if (hDist < 1.0e-6) {
            return;
        }

        applyRelativeMovement(
            player,
            physics,
            dx / hDist,
            dz / hDist,
            0.05,
            -0.80,
            0.08);
        physics.setBackward(false);
        physics.setSprint(false);
        physics.setJump(false);
    }

    private static float wrapDegrees(float value) {
        float wrapped = value % 360.0f;
        if (wrapped >= 180.0f) wrapped -= 360.0f;
        if (wrapped < -180.0f) wrapped += 360.0f;
        return wrapped;
    }
}
