package fr.riege.ebsl.common.feature.scripting.command;

import fr.riege.ebsl.common.feature.terminal.CommandContext;
import fr.riege.ebsl.common.feature.terminal.CommandResult;

@FunctionalInterface
interface EbslCommandActionHandler {
    CommandResult execute(CommandContext context);
}
