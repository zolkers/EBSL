package fr.riege.ebsl.pathfinding.goal;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public final class PathCommand {

    private PathCommand() {}

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        var root = ClientCommandManager.literal("pf");
        for (PathGoal goal : PathGoals.ALL) {
            root.then(goal.command());
        }
        dispatcher.register(root);
    }
}
