package fr.riege.ebsl.common.terminal.commands;

import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;
import fr.riege.ebsl.common.terminal.*;

@Command(name = "debug", description = "Toggle pathfinder debug overlay", usage = "debug", scope = CommandScope.BOTH)
public final class DebugCommand implements CommandHandler {
    @Override
    public CommandResult execute(CommandContext ctx) {
        boolean next = !PathfinderSettings.instance().showDebug.value();
        PathfinderSettings.instance().showDebug.setValue(next);
        return CommandResult.ok("Debug: " + (next ? "on" : "off"));
    }
}
