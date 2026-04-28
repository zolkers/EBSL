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
    private static final String BASE_TAG = "§b§le§3§lb§9§ls§d§ll";
    private static final String TAG_SEPARATOR = " §8>> ";
    private static final String TAG_END = "§8>> §r";

    private ClientUtils() {}

    public static void sendMessage(Minecraft mc, String message, boolean overlay) {
        if (mc.player == null) return;
        String formatted = overlay ? message : prefixed(message);
        if (overlay) {
            mc.gui.setOverlayMessage(Component.literal(formatted), false);
        } else {
            mc.player.displayClientMessage(Component.literal(formatted), false);
        }
    }

    public static void sendDebugMessage(Minecraft mc, String message) {
        sendTaggedMessage(mc, LogTag.DEBUG, message);
    }

    public static void sendTaggedMessage(Minecraft mc, LogTag tag, String message) {
        if (mc.player == null) return;
        mc.player.displayClientMessage(Component.literal(prefixed(tag, message)), false);
    }

    public static String basePrefix() {
        return BASE_TAG + TAG_END;
    }

    public static String prefixed(String message) {
        return basePrefix() + message;
    }

    public static String prefixed(LogTag tag, String message) {
        return BASE_TAG + TAG_SEPARATOR + tag.formattedName() + TAG_SEPARATOR + "§r" + message;
    }

    public enum LogTag {
        DEBUG("debug", "§7"),
        PATH("path", "§a"),
        WARN("warn", "§e"),
        ERROR("error", "§c");

        private final String label;
        private final String color;

        LogTag(String label, String color) {
            this.label = label;
            this.color = color;
        }

        String formattedName() {
            return color + label;
        }
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
