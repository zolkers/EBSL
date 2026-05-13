package fr.riege.ebsl.common.feature.terminal;

import fr.riege.ebsl.common.feature.terminal.goal.GoalParameter;

import java.util.List;
import java.util.Map;

/**
 * Handles one terminal command invocation.
 *
 * <p>Handlers receive a parsed command context and may provide parameters, completion rules, and nested subcommands.</p>
 */
public interface CommandHandler {
    /**
     * Executes the operation represented by this contract.
 *
     * @param ctx the command context
     * @return the value defined by this contract
     */
    CommandResult execute(CommandContext ctx);


    /**
     * Returns command parameters expected by this handler.
 *
     * @return the requested values
     */
    default List<GoalParameter> params() {
        return List.of();
    }


    /**
     * Returns the completion provider for this handler.
 *
     * @return the value defined by this contract
     */
    default CommandCompletion completer() {
        return CommandCompletion.EMPTY;
    }


    /**
     * Returns nested command handlers keyed by subcommand name.
 *
     * @return the value defined by this contract
     */
    default Map<String, CommandHandler> subcommands() {
        return Map.of();
    }
}
