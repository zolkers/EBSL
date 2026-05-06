package fr.riege.ebsl.common.terminal.commands;

import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;
import fr.riege.ebsl.common.service.EbslServices;
import fr.riege.ebsl.common.terminal.Command;
import fr.riege.ebsl.common.terminal.CommandContext;
import fr.riege.ebsl.common.terminal.CommandHandler;
import fr.riege.ebsl.common.terminal.CommandResult;
import fr.riege.ebsl.common.terminal.CommandScope;

@Command(name = "status", description = "Show navigation status", usage = "status", scope = CommandScope.MC)
public final class StatusCommand implements CommandHandler {
    @Override
    public CommandResult execute(CommandContext ctx) {
        String state = EbslServices.navigation().isNavigating() ? "navigating" : "idle";
        String debug = PathfinderSettings.instance().showDebug.value() ? "on" : "off";
        int jump = PathfinderSettings.instance().maxJumpHeight.value();
        return CommandResult.ok("state=" + state + " | jump=" + jump + " | debug=" + debug);
    }
}
