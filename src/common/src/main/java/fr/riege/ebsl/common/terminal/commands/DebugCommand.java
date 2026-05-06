package fr.riege.ebsl.common.terminal.commands;

import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;
import fr.riege.ebsl.common.terminal.Command;
import fr.riege.ebsl.common.terminal.CommandContext;
import fr.riege.ebsl.common.terminal.CommandHandler;
import fr.riege.ebsl.common.terminal.CommandResult;
import fr.riege.ebsl.common.terminal.CommandScope;

@Command(name = "debug", description = "Toggle pathfinder debug overlay", usage = "debug", scope = CommandScope.BOTH)
public final class DebugCommand implements CommandHandler {
    @Override
    public CommandResult execute(CommandContext ctx) {
        boolean next = !PathfinderSettings.instance().showDebug.value();
        PathfinderSettings.instance().showDebug.setValue(next);
        return CommandResult.ok("Debug: " + (next ? "on" : "off"));
    }
}
