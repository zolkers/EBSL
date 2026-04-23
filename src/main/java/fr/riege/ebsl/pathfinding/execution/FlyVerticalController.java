package fr.riege.ebsl.pathfinding.execution;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

final class FlyVerticalController {
    private static final double RAY_DIST = 2.5;

    void adjustVerticalKeysWithRaycast(Minecraft mc, Vec3 pos, double waypointY) {
        if (mc.player == null || mc.level == null) {
            return;
        }

        double dy = waypointY - pos.y;
        if (dy > 0.75) {
            mc.options.keyJump.setDown(true);
            mc.options.keyShift.setDown(false);
            return;
        }
        if (dy < -0.75 && mc.player.getAbilities().flying) {
            mc.options.keyShift.setDown(true);
            mc.options.keyJump.setDown(false);
            return;
        }

        float yaw = (float) Math.toRadians(mc.player.getYRot());
        double lookX = -Math.sin(yaw);
        double lookZ = Math.cos(yaw);

        Vec3 feetPos = pos.add(0, 0.1, 0);
        Vec3 headPos = pos.add(0, mc.player.getBbHeight() - 0.1, 0);
        Vec3 feetEnd = feetPos.add(lookX * RAY_DIST, 0, lookZ * RAY_DIST);
        Vec3 headEnd = headPos.add(lookX * RAY_DIST, 0, lookZ * RAY_DIST);

        HitResult feetTrace = mc.level.clip(new ClipContext(
            feetPos, feetEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player));
        HitResult headTrace = mc.level.clip(new ClipContext(
            headPos, headEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player));

        boolean blockAtFeet = feetTrace.getType() == HitResult.Type.BLOCK;
        boolean blockAtHead = headTrace.getType() == HitResult.Type.BLOCK;

        if (blockAtFeet && !blockAtHead) {
            mc.options.keyJump.setDown(true);
            mc.options.keyShift.setDown(false);
        } else if (blockAtHead && !blockAtFeet) {
            mc.options.keyShift.setDown(true);
            mc.options.keyJump.setDown(false);
        } else {
            mc.options.keyJump.setDown(false);
            mc.options.keyShift.setDown(false);
        }
    }
}
