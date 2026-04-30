package fr.riege.ebsl.terminal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;

public final class PathCommand {

    private PathCommand() {}

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        var root = ClientCommandManager.literal("ebsl");
        for (CommandMeta meta : CommandRegistry.allMeta()) {
            if (meta.scope() == CommandScope.MC || meta.scope() == CommandScope.BOTH) {
                root.then(buildWrapper(meta.name()));
            }
        }
        dispatcher.register(root);
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> buildWrapper(String name) {
        return ClientCommandManager.literal(name)
            .executes(ctx -> {
                CommandRegistry.dispatch(name, Minecraft.getInstance());
                return 1;
            })
            .then(ClientCommandManager.argument("args", StringArgumentType.greedyString())
                .executes(ctx -> {
                    String args = StringArgumentType.getString(ctx, "args");
                    CommandRegistry.dispatch(name + " " + args, Minecraft.getInstance());
                    return 1;
                }));
    }
}
