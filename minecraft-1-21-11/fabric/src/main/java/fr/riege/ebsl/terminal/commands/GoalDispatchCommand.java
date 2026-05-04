package fr.riege.ebsl.terminal.commands;

import fr.riege.ebsl.terminal.CommandContext;
import fr.riege.ebsl.terminal.CommandHandler;
import fr.riege.ebsl.terminal.CommandResult;
import fr.riege.ebsl.terminal.goal.GoalUiCatalog;
import fr.riege.ebsl.terminal.goal.GoalUiDefinition;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public final class GoalDispatchCommand implements CommandHandler {

    private final Map<String, CommandHandler> subs;

    public GoalDispatchCommand() {
        Map<String, CommandHandler> map = new LinkedHashMap<>();
        for (GoalUiDefinition def : GoalUiCatalog.all()) {
            map.put(def.id(), new GoalTerminalCommand(def));
        }
        subs = Map.copyOf(map);
    }

    @Override
    public CommandResult execute(CommandContext ctx) {
        if (ctx.argCount() == 0) {
            String names = subs.keySet().stream().collect(Collectors.joining(", "));
            return CommandResult.ok("Goals: " + names);
        }
        String goalId = ctx.arg(0);
        CommandHandler sub = subs.get(goalId.toLowerCase());
        if (sub == null) {
            return CommandResult.error("Unknown goal: " + goalId + "  (type 'goal' for list)");
        }
        return sub.execute(ctx.shift(1));
    }

    @Override
    public Map<String, CommandHandler> subcommands() {
        return subs;
    }
}
