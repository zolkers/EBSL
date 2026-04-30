package fr.riege.ebsl.terminal.commands;

import fr.riege.ebsl.terminal.goal.GoalParameter;
import fr.riege.ebsl.terminal.goal.GoalUiDefinition;
import fr.riege.ebsl.terminal.CommandContext;
import fr.riege.ebsl.terminal.CommandHandler;
import fr.riege.ebsl.terminal.CommandResult;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class GoalTerminalCommand implements CommandHandler {

    private final GoalUiDefinition def;

    public GoalTerminalCommand(GoalUiDefinition def) {
        this.def = def;
    }

    @Override
    public CommandResult execute(CommandContext ctx) {
        List<GoalParameter> params = def.parameters();
        if (ctx.argCount() != params.size()) {
            return CommandResult.badUsage(buildUsage());
        }
        Map<String, Integer> values = new LinkedHashMap<>();
        for (int i = 0; i < params.size(); i++) {
            values.put(params.get(i).id(), ctx.argInt(i));
        }
        def.execute(values);
        return CommandResult.ok(def.label() + " started");
    }

    @Override
    public List<GoalParameter> params() {
        return def.parameters();
    }

    private String buildUsage() {
        StringBuilder sb = new StringBuilder(def.id());
        for (GoalParameter p : def.parameters()) {
            sb.append(" <").append(p.label()).append('>');
        }
        return sb.toString();
    }
}
