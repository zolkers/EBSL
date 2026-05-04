package fr.riege.ebsl.terminal.commands;

import fr.riege.ebsl.pathfinding.settings.PathfinderSettings;
import fr.riege.ebsl.terminal.Command;
import fr.riege.ebsl.terminal.CommandContext;
import fr.riege.ebsl.terminal.CommandHandler;
import fr.riege.ebsl.terminal.CommandResult;
import fr.riege.ebsl.terminal.CommandScope;

@Command(name = "debug", description = "Toggle pathfinder debug overlay", usage = "debug", scope = CommandScope.BOTH)
public final class DebugCommand implements CommandHandler {

    @Override
    public CommandResult execute(CommandContext ctx) {
        boolean next = !PathfinderSettings.instance().showDebug.value();
        PathfinderSettings.instance().showDebug.setValue(next);
        return CommandResult.ok("Debug: " + (next ? "on" : "off"));
    }
}
