package fr.riege.ebsl.pathfinding.goal;

import fr.riege.ebsl.pathfinding.PathfinderConfig;
import fr.riege.ebsl.pathfinding.PathfindingManager;
import fr.riege.ebsl.pathfinding.debug.PathVisualizer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;

import java.util.StringJoiner;

public final class UtilityGoalCommands {
    private UtilityGoalCommands() {
    }

    public static void register() {
        GoalRegistry.register(new SimpleGoalCommandDefinition("test", () ->
            ClientCommandManager.literal("test")
                .then(GoalCommandSupport.intArg("x")
                .then(GoalCommandSupport.intArg("y")
                .then(GoalCommandSupport.intArg("z")
                .executes(ctx -> {
                    PathfindingManager.startPathTest(
                        GoalCommandSupport.minecraft(),
                        GoalCommandSupport.getInt(ctx, "x"),
                        GoalCommandSupport.getInt(ctx, "y"),
                        GoalCommandSupport.getInt(ctx, "z"));
                    return 1;
                }))))));

        GoalRegistry.register(new SimpleGoalCommandDefinition("stop", () ->
            ClientCommandManager.literal("stop").executes(ctx -> {
                PathfindingManager.stop();
                return 1;
            })));

        GoalRegistry.register(new SimpleGoalCommandDefinition("viz", () ->
            ClientCommandManager.literal("viz").executes(ctx -> {
                PathVisualizer.toggle();
                GoalCommandSupport.sendClientMessage("Visualizer: " + (PathVisualizer.isEnabled() ? "ON" : "OFF"));
                return 1;
            })));

        GoalRegistry.register(new SimpleGoalCommandDefinition("debug", () ->
            ClientCommandManager.literal("debug").executes(ctx -> {
                boolean next = !PathfinderConfig.SHOW_DEBUG.get();
                PathfinderConfig.SHOW_DEBUG.set(next);
                GoalCommandSupport.sendClientMessage("Debug: " + (next ? "ON" : "OFF"));
                return 1;
            })));

        GoalRegistry.register(new SimpleGoalCommandDefinition("jumpheight", () ->
            ClientCommandManager.literal("jumpheight")
                .then(GoalCommandSupport.boundedIntArg("n", 1, 5)
                .executes(ctx -> {
                    int n = GoalCommandSupport.getInt(ctx, "n");
                    PathfinderConfig.PATHFINDER_MAX_JUMP_HEIGHT.set(n);
                    GoalCommandSupport.sendClientMessage("Max jump height: " + n);
                    return 1;
                }))));

        GoalRegistry.register(new SimpleGoalCommandDefinition("status", () ->
            ClientCommandManager.literal("status").executes(ctx -> {
                if (GoalCommandSupport.minecraft().player == null) {
                    return 0;
                }
                String navigating = PathfindingManager.isNavigating() ? "§anavigating" : "§7idle";
                String debug = PathfinderConfig.SHOW_DEBUG.get() ? "§aON" : "§7OFF";
                String viz = PathVisualizer.isEnabled() ? "§aON" : "§7OFF";
                GoalCommandSupport.sendClientMessage(
                    navigating + "§r | jump=" + PathfinderConfig.PATHFINDER_MAX_JUMP_HEIGHT.get()
                        + " | debug=" + debug
                        + "§r | viz=" + viz);
                return 1;
            })));

        GoalRegistry.register(new SimpleGoalCommandDefinition("goals", () ->
            ClientCommandManager.literal("goals").executes(ctx -> {
                StringJoiner joiner = new StringJoiner(", ");
                for (String id : GoalRegistry.commandIds()) {
                    joiner.add(id);
                }
                GoalCommandSupport.sendClientMessage("Registered goals (" + GoalRegistry.commands().size() + "): " + joiner);
                return 1;
            })));
    }
}
