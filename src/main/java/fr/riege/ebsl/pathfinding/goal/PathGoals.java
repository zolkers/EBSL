package fr.riege.ebsl.pathfinding.goal;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import fr.riege.ebsl.pathfinding.PathfinderConfig;
import fr.riege.ebsl.pathfinding.PathfindingManager;
import fr.riege.ebsl.pathfinding.debug.PathVisualizer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.minecraft.client.Minecraft;

import java.util.List;

public final class PathGoals {

    public static final List<PathGoal> ALL = List.of(

        // /pf walk <x> <y> <z>  — A* walk
        () -> ClientCommandManager.literal("walk")
            .then(ClientCommandManager.argument("x", IntegerArgumentType.integer())
            .then(ClientCommandManager.argument("y", IntegerArgumentType.integer())
            .then(ClientCommandManager.argument("z", IntegerArgumentType.integer())
            .executes(ctx -> {
                Minecraft mc = Minecraft.getInstance();
                PathfindingManager.startPathfind(mc,
                    IntegerArgumentType.getInteger(ctx, "x"),
                    IntegerArgumentType.getInteger(ctx, "y"),
                    IntegerArgumentType.getInteger(ctx, "z"));
                return 1;
            })))),

        // /pf fly <x> <y> <z>  — direct fly path
        () -> ClientCommandManager.literal("fly")
            .then(ClientCommandManager.argument("x", IntegerArgumentType.integer())
            .then(ClientCommandManager.argument("y", IntegerArgumentType.integer())
            .then(ClientCommandManager.argument("z", IntegerArgumentType.integer())
            .executes(ctx -> {
                Minecraft mc = Minecraft.getInstance();
                PathfindingManager.startFlyPathfind(mc,
                    IntegerArgumentType.getInteger(ctx, "x"),
                    IntegerArgumentType.getInteger(ctx, "y"),
                    IntegerArgumentType.getInteger(ctx, "z"));
                return 1;
            })))),

        // /pf test <x> <y> <z>  — visualize A* without moving
        () -> ClientCommandManager.literal("test")
            .then(ClientCommandManager.argument("x", IntegerArgumentType.integer())
            .then(ClientCommandManager.argument("y", IntegerArgumentType.integer())
            .then(ClientCommandManager.argument("z", IntegerArgumentType.integer())
            .executes(ctx -> {
                Minecraft mc = Minecraft.getInstance();
                PathfindingManager.startPathTest(mc,
                    IntegerArgumentType.getInteger(ctx, "x"),
                    IntegerArgumentType.getInteger(ctx, "y"),
                    IntegerArgumentType.getInteger(ctx, "z"));
                return 1;
            })))),

        // /pf precise <x> <y> <z>  — walk with 0.1 goal tolerance
        () -> ClientCommandManager.literal("precise")
            .then(ClientCommandManager.argument("x", IntegerArgumentType.integer())
            .then(ClientCommandManager.argument("y", IntegerArgumentType.integer())
            .then(ClientCommandManager.argument("z", IntegerArgumentType.integer())
            .executes(ctx -> {
                Minecraft mc = Minecraft.getInstance();
                PathfindingManager.startConfiguredWalk(mc,
                    IntegerArgumentType.getInteger(ctx, "x"),
                    IntegerArgumentType.getInteger(ctx, "y"),
                    IntegerArgumentType.getInteger(ctx, "z"),
                    null, null, true, 0.1);
                return 1;
            })))),

        // /pf noreplan <x> <y> <z>  — walk with replanning disabled
        () -> ClientCommandManager.literal("noreplan")
            .then(ClientCommandManager.argument("x", IntegerArgumentType.integer())
            .then(ClientCommandManager.argument("y", IntegerArgumentType.integer())
            .then(ClientCommandManager.argument("z", IntegerArgumentType.integer())
            .executes(ctx -> {
                Minecraft mc = Minecraft.getInstance();
                PathfindingManager.startConfiguredWalk(mc,
                    IntegerArgumentType.getInteger(ctx, "x"),
                    IntegerArgumentType.getInteger(ctx, "y"),
                    IntegerArgumentType.getInteger(ctx, "z"),
                    null, null, false, 0.5);
                return 1;
            })))),

        // /pf stop  — stop current navigation
        () -> ClientCommandManager.literal("stop")
            .executes(ctx -> {
                PathfindingManager.stop();
                return 1;
            }),

        // /pf viz  — toggle path visualizer
        () -> ClientCommandManager.literal("viz")
            .executes(ctx -> {
                PathVisualizer.toggle();
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    mc.player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                        "[pf] Visualizer: " + (PathVisualizer.isEnabled() ? "ON" : "OFF")), false);
                }
                return 1;
            }),

        // /pf debug  — toggle debug messages
        () -> ClientCommandManager.literal("debug")
            .executes(ctx -> {
                boolean next = !PathfinderConfig.SHOW_DEBUG.get();
                PathfinderConfig.SHOW_DEBUG.set(next);
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    mc.player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                        "[pf] Debug: " + (next ? "ON" : "OFF")), false);
                }
                return 1;
            }),

        // /pf jumpheight <n>  — set max jump height (1-5)
        () -> ClientCommandManager.literal("jumpheight")
            .then(ClientCommandManager.argument("n", IntegerArgumentType.integer(1, 5))
            .executes(ctx -> {
                int n = IntegerArgumentType.getInteger(ctx, "n");
                PathfinderConfig.PATHFINDER_MAX_JUMP_HEIGHT.set(n);
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    mc.player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                        "[pf] Max jump height: " + n), false);
                }
                return 1;
            })),

        // /pf status  — print current navigation state
        () -> ClientCommandManager.literal("status")
            .executes(ctx -> {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player == null) return 0;
                String navigating = PathfindingManager.isNavigating() ? "§anavigating" : "§7idle";
                String debug = PathfinderConfig.SHOW_DEBUG.get() ? "§aON" : "§7OFF";
                String viz   = PathVisualizer.isEnabled() ? "§aON" : "§7OFF";
                mc.player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                    "§e[pf] §r" + navigating
                    + "§r | jump=" + PathfinderConfig.PATHFINDER_MAX_JUMP_HEIGHT.get()
                    + " | debug=" + debug
                    + "§r | viz=" + viz), false);
                return 1;
            })
    );

    private PathGoals() {}
}
