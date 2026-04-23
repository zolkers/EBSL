package fr.riege.ebsl.pathfinding.execution;

import fr.riege.ebsl.pathfinding.PathfinderConfig;
import fr.riege.ebsl.pathfinding.rotation.AngleUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

final class FlyRotationController {
    private float smoothedYaw = Float.MAX_VALUE;

    void reset() {
        smoothedYaw = Float.MAX_VALUE;
    }

    void rotateWaypointYaw(Minecraft mc, double dx, double dz, Consumer<String> debug) {
        if (mc.player == null) {
            return;
        }
        double horizDist = Math.sqrt(dx * dx + dz * dz);
        if (horizDist < 3.0) {
            debug(debug, "setHorizontalRotation skipped horizDist=%.2f dx=%.2f dz=%.2f",
                horizDist, dx, dz);
            return;
        }

        float targetYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        if (smoothedYaw == Float.MAX_VALUE) {
            smoothedYaw = mc.player.getYRot();
            debug(debug, "setHorizontalRotation seed smoothedYaw=%.2f", smoothedYaw);
        }

        float diff = Mth.wrapDegrees(targetYaw - smoothedYaw);
        smoothedYaw += diff * 0.25f;
        debug(debug, "setHorizontalRotation prev=%.2f target=%.2f diff=%.2f smoothed=%.2f",
            mc.player.getYRot(), targetYaw, diff, smoothedYaw);

        float prevYaw = mc.player.getYRot();
        mc.player.setYRot(smoothedYaw);
        mc.player.yRotO = prevYaw;
        mc.player.yHeadRotO = prevYaw;
        mc.player.yBodyRotO = prevYaw;
        mc.player.yHeadRot = smoothedYaw;
        mc.player.yBodyRot = smoothedYaw;
    }

    void rotateTowardLookTarget(Minecraft mc, Vec3 target, Consumer<String> debug) {
        if (mc.player == null) {
            return;
        }
        Vec3 eye = mc.player.getEyePosition();
        double dx = target.x - eye.x;
        double dy = target.y - eye.y;
        double dz = target.z - eye.z;
        float wantYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float wantPitch = (float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
        float yawDiff = Math.abs(AngleUtils.getRotationDelta(mc.player.getYRot(), wantYaw));
        float pitchDiff = Math.abs(wantPitch - mc.player.getXRot());
        debug(debug, "rotateTowardLookTarget want=(yaw=%.2f,pitch=%.2f) current=(yaw=%.2f,pitch=%.2f) diff=(yaw=%.2f,pitch=%.2f)",
            wantYaw, wantPitch, mc.player.getYRot(), mc.player.getXRot(), yawDiff, pitchDiff);
        if (yawDiff > 4.0f || pitchDiff > 4.0f) {
            float newYaw = mc.player.getYRot() + AngleUtils.getRotationDelta(mc.player.getYRot(), wantYaw) * 0.25f;
            float newPitch = mc.player.getXRot() + (wantPitch - mc.player.getXRot()) * 0.25f;
            float prevYaw = mc.player.getYRot();
            float prevPitch = mc.player.getXRot();
            mc.player.setYRot(newYaw);
            mc.player.setXRot(Math.max(-90f, Math.min(90f, newPitch)));
            mc.player.yRotO = prevYaw;
            mc.player.xRotO = prevPitch;
            mc.player.yHeadRotO = prevYaw;
            mc.player.yBodyRotO = prevYaw;
            mc.player.yHeadRot = newYaw;
            mc.player.yBodyRot = newYaw;
            debug(debug, "rotateTowardLookTarget applied prev=(yaw=%.2f,pitch=%.2f) new=(yaw=%.2f,pitch=%.2f)",
                prevYaw, prevPitch, newYaw, newPitch);
            return;
        }

        debug(debug, "rotateTowardLookTarget skipped small diff");
    }

    private static void debug(Consumer<String> debug, String message, Object... args) {
        if (debug != null && PathfinderConfig.SHOW_DEBUG.get()) {
            debug.accept(String.format(message, args));
        }
    }
}
