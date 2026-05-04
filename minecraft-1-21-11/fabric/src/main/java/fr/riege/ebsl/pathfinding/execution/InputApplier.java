package fr.riege.ebsl.pathfinding.execution;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

final class InputApplier {
    private static final double STRAFE_SUPPRESSION_FORWARD_DOT = 0.45;
    private static final double STRAFE_SUPPRESSION_DOT = 0.55;

    private InputApplier() {
    }

    static void releaseAll(Minecraft mc, boolean keepSneaking) {
        if (mc == null) {
            return;
        }
        mc.options.keyUp.setDown(false);
        mc.options.keyDown.setDown(false);
        mc.options.keyLeft.setDown(false);
        mc.options.keyRight.setDown(false);
        mc.options.keyJump.setDown(mc.player != null && mc.player.isInWater());
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

        mc.options.keyUp.setDown(forwardDot > forwardThreshold);
        mc.options.keyDown.setDown(forwardDot < backwardThreshold);

        boolean mostlyForward = forwardDot > STRAFE_SUPPRESSION_FORWARD_DOT;
        double effectiveStrafeThreshold = mostlyForward
            ? Math.max(strafeThreshold, STRAFE_SUPPRESSION_DOT)
            : strafeThreshold;
        mc.options.keyRight.setDown(strafeDot > effectiveStrafeThreshold);
        mc.options.keyLeft.setDown(strafeDot < -effectiveStrafeThreshold);
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
        boolean inWater = mc.player.isInWater();
        mc.options.keyJump.setDown(inWater);
        mc.options.keyShift.setDown(!inWater);
    }

    static void applyCornerAlignment(Minecraft mc, double dx, double dz) {
        if (mc.player == null) {
            return;
        }

        double hDist = Math.sqrt(dx * dx + dz * dz);
        if (hDist < 1.0e-6) {
            return;
        }

        InputApplier.applyRelativeMovement(
            mc,
            dx / hDist,
            dz / hDist,
            0.05,
            -0.80,
            0.08);
        mc.options.keyDown.setDown(false);
        mc.options.keySprint.setDown(false);
        mc.options.keyJump.setDown(false);
    }
}
