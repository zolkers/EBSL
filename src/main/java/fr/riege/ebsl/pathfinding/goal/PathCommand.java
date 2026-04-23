package fr.riege.ebsl.pathfinding.goal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public final class PathCommand {

    private PathCommand() {}

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        GoalCommands.bootstrap();
        dispatcher.register(buildRoot("ebsl"));
        dispatcher.register(buildRoot("pf"));
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> buildRoot(String name) {
        var root = ClientCommandManager.literal(name);
        for (GoalCommandDefinition goal : GoalRegistry.commands()) {
            root.then(goal.command());
        }
        return root;
    }
}
