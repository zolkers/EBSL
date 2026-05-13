package fr.riege.ebsl.loader.layer;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fr.riege.ebsl.common.feature.terminal.CommandRegistry;
import fr.riege.ebsl.common.feature.terminal.CommandSuggestion;
import fr.riege.ebsl.common.platform.layer.ICommandLayer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MinecraftCommandLayer<S> implements ICommandLayer {
    private final Map<String, RegisteredCommand> commands = new LinkedHashMap<>();

    @Override
    public void register(String name, String description, CommandHandler handler) {
        String commandName = name.toLowerCase(Locale.ROOT);
        commands.put(commandName, new RegisteredCommand(commandName, description, handler));
    }

    @Override
    public void print(String message) {
        send(message);
    }

    @Override
    public void printError(String message) {
        send("§c" + message);
    }

    @Override
    public void printSuccess(String message) {
        send("§a" + message);
    }

    @Override
    public List<String> getSuggestions(String input) {
        return CommandRegistry.suggest(input).stream()
            .map(CommandSuggestion::fill)
            .toList();
    }

    public void registerAll(CommandDispatcher<S> dispatcher) {
        LiteralArgumentBuilder<S> root = LiteralArgumentBuilder.<S>literal("ebsl")
            .executes(context -> {
                print("§eEBSL commands: " + String.join(", ", commands.keySet()));
                return Command.SINGLE_SUCCESS;
            });

        for (RegisteredCommand command : commands.values()) {
            root.then(commandNode(command));
        }

        dispatcher.register(root);
    }

    private LiteralArgumentBuilder<S> commandNode(RegisteredCommand command) {
        return LiteralArgumentBuilder.<S>literal(command.name())
            .executes(context -> execute(command.handler(), new String[0]))
            .then(RequiredArgumentBuilder.<S, String>argument("args", StringArgumentType.greedyString())
                .suggests(suggestionsFor(command.name()))
                .executes(context -> execute(command.handler(), splitArgs(StringArgumentType.getString(context, "args")))));
    }

    private SuggestionProvider<S> suggestionsFor(String commandName) {
        return (context, builder) -> {
            String input = commandName + " " + remainingInput(context, builder);
            for (CommandSuggestion suggestion : CommandRegistry.suggest(input)) {
                if (suggestion.hint().isBlank()) {
                    builder.suggest(suggestion.fill());
                } else {
                    builder.suggest(suggestion.fill(), new LiteralMessage(suggestion.hint()));
                }
            }
            return builder.buildFuture();
        };
    }

    private static <S> String remainingInput(CommandContext<S> context, SuggestionsBuilder builder) {
        String fullInput = context.getInput();
        int start = Math.min(builder.getStart(), fullInput.length());
        return fullInput.substring(start);
    }

    private int execute(CommandHandler handler, String[] args) {
        handler.execute(args, this::print);
        return Command.SINGLE_SUCCESS;
    }

    private static String[] splitArgs(String input) {
        String trimmed = input.trim();
        return trimmed.isEmpty() ? new String[0] : trimmed.split("\\s+");
    }

    private static void send(String message) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.displayClientMessage(Component.literal(message), false);
        }
    }

    private record RegisteredCommand(String name, String description, CommandHandler handler) {
    }
}
