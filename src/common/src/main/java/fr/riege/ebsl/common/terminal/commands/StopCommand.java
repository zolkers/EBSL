package fr.riege.ebsl.common.terminal.commands;

import fr.riege.ebsl.common.service.EbslServices;
import fr.riege.ebsl.common.terminal.Command;
import fr.riege.ebsl.common.terminal.CommandContext;
import fr.riege.ebsl.common.terminal.CommandHandler;
import fr.riege.ebsl.common.terminal.CommandResult;
import fr.riege.ebsl.common.terminal.CommandScope;

@Command(name = "stop", description = "Stop navigation", scope = CommandScope.BOTH)
public final class StopCommand implements CommandHandler {
    @Override
    public CommandResult execute(CommandContext ctx) {
        boolean was = EbslServices.navigation().isNavigating();
        EbslServices.navigation().stop(false);
        return CommandResult.ok(was ? "Navigation stopped." : "Not navigating.");
    }
}
