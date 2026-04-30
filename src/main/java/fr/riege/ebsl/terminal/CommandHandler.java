package fr.riege.ebsl.terminal;

import fr.riege.ebsl.terminal.goal.GoalParameter;

import java.util.List;
import java.util.Map;

public interface CommandHandler {
    CommandResult execute(CommandContext ctx);

    /** Live parameter definitions used for dynamic suggestions. */
    default List<GoalParameter> params() {
        return List.of();
    }

    /** Static per-argument completion (fallback when params() is empty). */
    default CommandCompletion completer() {
        return CommandCompletion.EMPTY;
    }

    /** Named sub-handlers (e.g. goal walk, goal fly). Used for Brigadier tree + suggestions. */
    default Map<String, CommandHandler> subcommands() {
        return Map.of();
    }
}
