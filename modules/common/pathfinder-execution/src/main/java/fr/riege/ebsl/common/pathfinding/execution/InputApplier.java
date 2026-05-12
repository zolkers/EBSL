package fr.riege.ebsl.common.pathfinding.execution;

import fr.riege.ebsl.common.platform.layer.IInputLayer;
import fr.riege.ebsl.common.world.layer.IPlayerLayer;

final class InputApplier {
    private static final double STRAFE_SUPPRESSION_FORWARD_DOT = 0.45;
    private static final double STRAFE_SUPPRESSION_DOT = 0.55;

    private InputApplier() {
    }

    static void releaseAll(IInputLayer input, boolean keepSneaking) {
        input.releaseMovementKeys();
        input.setSneakDown(keepSneaking);
    }

    static void applyRelativeMovement(IPlayerLayer player, IInputLayer input, double dx, double dz,
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

        input.setForwardDown(forwardDot > forwardThreshold);
        input.setBackwardDown(forwardDot < backwardThreshold);

        boolean mostlyForward = forwardDot > STRAFE_SUPPRESSION_FORWARD_DOT;
        double effectiveStrafeThreshold = mostlyForward
            ? Math.max(strafeThreshold, STRAFE_SUPPRESSION_DOT)
            : strafeThreshold;
        input.setRightDown(strafeDot > effectiveStrafeThreshold);
        input.setLeftDown(strafeDot < -effectiveStrafeThreshold);
    }

    static void applyGoalCentering(IPlayerLayer player, IInputLayer input, double dx, double dz) {
        float yawToGoal = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float relativeYaw = wrapDegrees(yawToGoal - player.yaw());

        input.setForwardDown(relativeYaw > -67.5 && relativeYaw <= 67.5);
        input.setBackwardDown(relativeYaw > 112.5 || relativeYaw <= -112.5);
        input.setLeftDown(relativeYaw > -157.5 && relativeYaw <= -22.5);
        input.setRightDown(relativeYaw > 22.5 && relativeYaw <= 157.5);
        input.setSprintDown(false);
        boolean inWater = player.isInWater();
        input.setJumpDown(inWater);
        input.setSneakDown(!inWater);
    }

    static void applyCornerAlignment(IPlayerLayer player, IInputLayer input, double dx, double dz) {
        double hDist = Math.sqrt(dx * dx + dz * dz);
        if (hDist < 1.0e-6) {
            return;
        }

        applyRelativeMovement(
            player,
            input,
            dx / hDist,
            dz / hDist,
            0.05,
            -0.80,
            0.08);
        input.setBackwardDown(false);
        input.setSprintDown(false);
        input.setJumpDown(false);
    }

    private static float wrapDegrees(float value) {
        float wrapped = value % 360.0f;
        if (wrapped >= 180.0f) wrapped -= 360.0f;
        if (wrapped < -180.0f) wrapped += 360.0f;
        return wrapped;
    }
}
