package fr.riege.ebsl.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.InteractionHand;

public final class ClientUtils {

    private ClientUtils() {}

    public static void sendMessage(Minecraft mc, String message, boolean overlay) {
        if (mc.player == null) return;
        if (overlay) {
            mc.gui.setOverlayMessage(Component.literal(message), false);
        } else {
            mc.player.displayClientMessage(Component.literal(message), false);
        }
    }

    public static void sendDebugMessage(Minecraft mc, String message) {
        sendMessage(mc, "[debug] " + message, false);
    }

    public static boolean isInventoryScreenOpen(Minecraft mc) {
        return mc.screen instanceof AbstractContainerScreen;
    }

    public static void performUseClick(Minecraft mc) {
        if (mc.player == null || mc.gameMode == null) return;
        mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
    }

    public static boolean hasLineOfSight(Entity entity, Vec3 target) {
        Vec3 eyePos = entity.getEyePosition();
        ClipContext ctx = new ClipContext(eyePos, target,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity);
        BlockHitResult result = entity.level().clip(ctx);
        return result.getType() == HitResult.Type.MISS;
    }
}
