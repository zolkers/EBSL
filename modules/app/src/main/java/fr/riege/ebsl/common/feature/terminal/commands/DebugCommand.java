package fr.riege.ebsl.common.feature.terminal.commands;

import fr.riege.ebsl.common.feature.terminal.*;
import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;

@Command(name = CommandIds.DEBUG, description = "Toggle pathfinder debug overlay", usage = CommandIds.DEBUG, scope = CommandScope.BOTH)
public final class DebugCommand implements CommandHandler {
    @Override
    public CommandResult execute(CommandContext ctx) {
        boolean next = !PathfinderSettings.instance().showDebug.value();
        PathfinderSettings.instance().showDebug.setValue(next);
        return CommandResult.ok("Debug: " + (next ? "on" : "off"));
    }
}
