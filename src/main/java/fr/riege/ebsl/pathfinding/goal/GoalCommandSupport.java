package fr.riege.ebsl.pathfinding.goal;

import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import fr.riege.ebsl.pathfinding.PathfindingManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class GoalCommandSupport {
    public static final String PREFIX = "§e[ebsl] §r";

    private GoalCommandSupport() {
    }

    public static Minecraft minecraft() {
        return Minecraft.getInstance();
    }

    public static int startNavigation(NavigationRequest request) {
        PathfindingManager.startGoal(minecraft(), request);
        return 1;
    }

    public static void sendClientMessage(String message) {
        Minecraft mc = minecraft();
        if (mc.player != null) {
            mc.player.displayClientMessage(Component.literal(PREFIX + message), false);
        }
    }

    public static int currentBlockX() {
        return (int) Math.floor(minecraft().player.getX());
    }

    public static int currentBlockY() {
        return (int) Math.floor(minecraft().player.getY());
    }

    public static int currentBlockZ() {
        return (int) Math.floor(minecraft().player.getZ());
    }

    public static RequiredArgumentBuilder<FabricClientCommandSource, Integer> intArg(String name) {
        return ClientCommandManager.argument(name, com.mojang.brigadier.arguments.IntegerArgumentType.integer());
    }

    public static RequiredArgumentBuilder<FabricClientCommandSource, Integer> boundedIntArg(String name, int min, int max) {
        return ClientCommandManager.argument(name, com.mojang.brigadier.arguments.IntegerArgumentType.integer(min, max));
    }

    public static int getInt(CommandContext<FabricClientCommandSource> ctx, String name) {
        return com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(ctx, name);
    }
}
