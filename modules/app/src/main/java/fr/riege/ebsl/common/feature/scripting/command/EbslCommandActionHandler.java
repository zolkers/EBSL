package fr.riege.ebsl.common.feature.scripting.command;

import fr.riege.ebsl.common.feature.terminal.CommandContext;
import fr.riege.ebsl.common.feature.terminal.CommandResult;

/**
 * Executes one action exposed by the EBSL scripting command bridge.
 *
 * <p>Implementations translate terminal context into command results and should avoid retaining invocation-specific state.</p>
 */
@FunctionalInterface
interface EbslCommandActionHandler {
    /**
     * Executes the operation represented by this contract.
 *
     * @param context the context describing the operation being performed
     * @return the value defined by this contract
     */
    CommandResult execute(CommandContext context);
}
