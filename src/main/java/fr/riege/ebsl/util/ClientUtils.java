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
    private static final String TAG_SEPARATOR = " §8>> §r";

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
        return ChatTagBuilder.create().append(BASE_TAG).finish();
    }

    public static String prefixed(String message) {
        return ChatTagBuilder.create().append(BASE_TAG).finish(message);
    }

    public static String prefixed(LogTag tag, String message) {
        return ChatTagBuilder.create()
            .append(BASE_TAG)
            .append(tag.formattedName())
            .finish(message);
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

    private static final class ChatTagBuilder {
        private final StringBuilder builder = new StringBuilder();
        private boolean hasTag;

        static ChatTagBuilder create() {
            return new ChatTagBuilder();
        }

        ChatTagBuilder append(String formattedTag) {
            if (hasTag) {
                builder.append(TAG_SEPARATOR);
            }
            builder.append(formattedTag);
            hasTag = true;
            return this;
        }

        String finish() {
            return builder.append(TAG_SEPARATOR).toString();
        }

        String finish(String message) {
            return finish() + message;
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
