package fr.riege.ebsl.common.feature.terminal.commands;

import fr.riege.ebsl.common.feature.terminal.*;
import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;
import fr.riege.ebsl.common.platform.service.EbslServices;

@Command(name = CommandIds.STATUS, description = "Show navigation status", usage = CommandIds.STATUS, scope = CommandScope.MC)
public final class StatusCommand implements CommandHandler {
    @Override
    public CommandResult execute(CommandContext ctx) {
        String state = EbslServices.navigation().isNavigating() ? "navigating" : "idle";
        String debug = Boolean.TRUE.equals(PathfinderSettings.instance().showDebug.value()) ? "on" : "off";
        int jump = PathfinderSettings.instance().maxJumpHeight.value();
        return CommandResult.ok("state=" + state + " | jump=" + jump + " | debug=" + debug);
    }
}
