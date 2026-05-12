package fr.riege.ebsl.common.feature.terminal.commands;

import fr.riege.ebsl.common.platform.service.EbslServices;
import fr.riege.ebsl.common.feature.terminal.*;

@Command(name = CommandIds.STOP, description = "Stop navigation", scope = CommandScope.BOTH)
public final class StopCommand implements CommandHandler {
    @Override
    public CommandResult execute(CommandContext ctx) {
        boolean was = EbslServices.navigation().isNavigating();
        EbslServices.navigation().stop(false);
        return CommandResult.ok(was ? "Navigation stopped." : "Not navigating.");
    }
}
