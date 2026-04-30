package fr.riege.ebsl.terminal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import fr.riege.ebsl.terminal.goal.GoalParameter;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;

import java.util.List;
import java.util.Map;

public final class PathCommand {

    private PathCommand() {}

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        var root = ClientCommandManager.literal("ebsl");
        for (CommandMeta meta : CommandRegistry.allMeta()) {
            if (meta.scope() == CommandScope.MC || meta.scope() == CommandScope.BOTH) {
                CommandHandler handler = CommandRegistry.handler(meta.name());
                root.then(buildWrapper(meta.name(), handler));
            }
        }
        dispatcher.register(root);
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> buildWrapper(
            String name, CommandHandler handler) {
        var literal = ClientCommandManager.literal(name);
        if (handler == null) {
            return literal.executes(ctx -> { CommandRegistry.dispatch(name, Minecraft.getInstance()); return 1; });
        }

        Map<String, CommandHandler> subs = handler.subcommands();
        if (!subs.isEmpty()) {
            // e.g. "goal" → ebsl goal <walk|fly|...> [params]
            literal.executes(ctx -> { CommandRegistry.dispatch(name, Minecraft.getInstance()); return 1; });
            for (Map.Entry<String, CommandHandler> sub : subs.entrySet()) {
                String subName = sub.getKey();
                List<GoalParameter> subParams = sub.getValue().params();
                var subLiteral = ClientCommandManager.literal(subName);
                if (subParams.isEmpty()) {
                    subLiteral.executes(ctx -> {
                        CommandRegistry.dispatch(name + " " + subName, Minecraft.getInstance());
                        return 1;
                    });
                } else {
                    subLiteral.then(buildParamChain(name + " " + subName, subParams, 0));
                }
                literal.then(subLiteral);
            }
            return literal;
        }

        List<GoalParameter> params = handler.params();
        if (!params.isEmpty()) {
            // typed integer argument chain with live default suggestions
            literal.then(buildParamChain(name, params, 0));
        } else {
            // no typed params: literal dispatch + greedy fallback
            literal.executes(ctx -> { CommandRegistry.dispatch(name, Minecraft.getInstance()); return 1; });
            literal.then(ClientCommandManager.argument("args", StringArgumentType.greedyString())
                .executes(ctx -> {
                    CommandRegistry.dispatch(
                        name + " " + StringArgumentType.getString(ctx, "args"),
                        Minecraft.getInstance());
                    return 1;
                }));
        }

        return literal;
    }

    private static RequiredArgumentBuilder<FabricClientCommandSource, Integer> buildParamChain(
            String name, List<GoalParameter> params, int idx) {
        final int fi = idx;
        var arg = ClientCommandManager
            .argument(params.get(idx).label(), IntegerArgumentType.integer())
            .suggests((ctx, builder) -> {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    builder.suggest(params.get(fi).defaultValue(mc));
                }
                return builder.buildFuture();
            })
            .executes(ctx -> {
                StringBuilder sb = new StringBuilder(name);
                for (int i = 0; i <= fi; i++) {
                    sb.append(' ').append(IntegerArgumentType.getInteger(ctx, params.get(i).label()));
                }
                CommandRegistry.dispatch(sb.toString(), Minecraft.getInstance());
                return 1;
            });

        if (idx + 1 < params.size()) {
            arg.then(buildParamChain(name, params, idx + 1));
        }

        return arg;
    }
}
