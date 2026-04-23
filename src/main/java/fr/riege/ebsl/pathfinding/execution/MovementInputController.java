package fr.riege.ebsl.pathfinding.execution;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

final class MovementInputController {
    private static final double RELEASE_HYSTERESIS = 0.10;

    private MovementInputController() {
    }

    static void releaseAll(Minecraft mc, boolean keepSneaking) {
        if (mc == null || mc.options == null) {
            return;
        }
        mc.options.keyUp.setDown(false);
        mc.options.keyDown.setDown(false);
        mc.options.keyLeft.setDown(false);
        mc.options.keyRight.setDown(false);
        mc.options.keyJump.setDown(false);
        mc.options.keySprint.setDown(false);
        mc.options.keyShift.setDown(keepSneaking);
    }

    static void applyRelativeMovement(Minecraft mc, double dx, double dz,
                                      double forwardThreshold,
                                      double backwardThreshold,
                                      double strafeThreshold) {
        if (mc.player == null) {
            return;
        }

        float yawRad = (float) Math.toRadians(mc.player.getYRot());
        double forwardX = -Math.sin(yawRad);
        double forwardZ = Math.cos(yawRad);
        double rightX = -Math.sin(yawRad + Math.PI / 2.0);
        double rightZ = Math.cos(yawRad + Math.PI / 2.0);

        double forwardDot = dx * forwardX + dz * forwardZ;
        double strafeDot = dx * rightX + dz * rightZ;

        applyHysteresis(mc.options.keyUp, forwardDot, forwardThreshold, forwardThreshold - RELEASE_HYSTERESIS);
        applyHysteresis(mc.options.keyDown, -forwardDot, -backwardThreshold, -backwardThreshold - RELEASE_HYSTERESIS);
        applyHysteresis(mc.options.keyRight, strafeDot, strafeThreshold, strafeThreshold - RELEASE_HYSTERESIS);
        applyHysteresis(mc.options.keyLeft, -strafeDot, strafeThreshold, strafeThreshold - RELEASE_HYSTERESIS);

        if (mc.options.keyLeft.isDown() && mc.options.keyRight.isDown()) {
            if (strafeDot >= 0.0) {
                mc.options.keyLeft.setDown(false);
            } else {
                mc.options.keyRight.setDown(false);
            }
        }
        if (mc.options.keyUp.isDown() && mc.options.keyDown.isDown()) {
            if (forwardDot >= 0.0) {
                mc.options.keyDown.setDown(false);
            } else {
                mc.options.keyUp.setDown(false);
            }
        }
    }

    static void applyGoalCentering(Minecraft mc, double dx, double dz) {
        if (mc.player == null) {
            return;
        }

        float yawToGoal = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float relativeYaw = Mth.wrapDegrees(yawToGoal - mc.player.getYRot());

        mc.options.keyUp.setDown(relativeYaw > -67.5 && relativeYaw <= 67.5);
        mc.options.keyDown.setDown(relativeYaw > 112.5 || relativeYaw <= -112.5);
        mc.options.keyLeft.setDown(relativeYaw > -157.5 && relativeYaw <= -22.5);
        mc.options.keyRight.setDown(relativeYaw > 22.5 && relativeYaw <= 157.5);
        mc.options.keySprint.setDown(false);
        mc.options.keyJump.setDown(false);
        mc.options.keyShift.setDown(true);
    }

    private static void applyHysteresis(net.minecraft.client.KeyMapping key, double value,
                                        double pressThreshold, double releaseThreshold) {
        if (key.isDown()) {
            key.setDown(value > releaseThreshold);
            return;
        }
        key.setDown(value > pressThreshold);
    }
}
