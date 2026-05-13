package fr.riege.ebsl.common.feature.terminal;

import fr.riege.ebsl.common.feature.terminal.goal.GoalParameter;

import java.util.List;
import java.util.Map;

public interface CommandHandler {
    CommandResult execute(CommandContext ctx);


    default List<GoalParameter> params() {
        return List.of();
    }


    default CommandCompletion completer() {
        return CommandCompletion.EMPTY;
    }


    default Map<String, CommandHandler> subcommands() {
        return Map.of();
    }
}
