package fr.riege.ebsl.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import fr.riege.ebsl.pathfinding.PathfindingManager;
import fr.riege.ebsl.pathfinding.settings.PathfinderSettings;
import fr.riege.ebsl.ui.imgui.EbslImGuiOverlay;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import java.util.StringJoiner;

public final class PathCommand {

    private PathCommand() {}

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        GoalCommands.bootstrap();
        dispatcher.register(buildRoot());
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> buildRoot() {
        return ClientCommandManager.literal("ebsl")
            .then(ClientCommandManager.literal("ui").executes(ctx -> {
                boolean visible = EbslImGuiOverlay.toggle();
                GoalCommandSupport.sendClientMessage("EBSL UI overlay: " + (visible ? "ON" : "OFF"));
                return 1;
            }))
            .then(buildGoalRoot())
            .then(buildPathRoot());
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> buildGoalRoot() {
        var root = ClientCommandManager.literal("goal");
        for (GoalCommandDefinition goal : GoalRegistry.commands()) {
            root.then(goal.command());
        }
        root.then(ClientCommandManager.literal("list").executes(ctx -> {
            StringJoiner joiner = new StringJoiner(", ");
            for (String id : GoalRegistry.commandIds()) {
                joiner.add(id);
            }
            GoalCommandSupport.sendClientMessage(
                "Registered goals (" + GoalRegistry.commands().size() + "): " + joiner);
            return 1;
        }));
        return root;
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> buildPathRoot() {
        var root = ClientCommandManager.literal("path");
        root.then(ClientCommandManager.literal("cancel").executes(ctx -> {
            PathfindingManager.stop();
            return 1;
        }));
        root.then(ClientCommandManager.literal("status").executes(ctx -> {
            if (GoalCommandSupport.minecraft().player == null) {
                return 0;
            }
            String navigating = PathfindingManager.isNavigating() ? "§anavigating" : "§7idle";
            String debug = PathfinderSettings.instance().showDebug.value() ? "§aON" : "§7OFF";
            GoalCommandSupport.sendClientMessage(
                navigating + "§r | jump=" + PathfinderSettings.instance().maxJumpHeight.value()
                    + " | debug=" + debug
                    + "§r | visualizer=§aalways on");
            return 1;
        }));
        root.then(ClientCommandManager.literal("debug").executes(ctx -> {
            boolean next = !PathfinderSettings.instance().showDebug.value();
            PathfinderSettings.instance().showDebug.setValue(next);
            GoalCommandSupport.sendClientMessage("Debug: " + (next ? "ON" : "OFF"));
            return 1;
        }));
        root.then(ClientCommandManager.literal("jumpheight")
            .then(GoalCommandSupport.boundedIntArg("n", 1, 20)
            .executes(ctx -> {
                int n = GoalCommandSupport.getInt(ctx, "n");
                PathfinderSettings.instance().maxJumpHeight.setValue(n);
                GoalCommandSupport.sendClientMessage("Max jump height: " + n);
                return 1;
            })));
        root.then(ClientCommandManager.literal("test")
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
            })))));
        return root;
    }
}
