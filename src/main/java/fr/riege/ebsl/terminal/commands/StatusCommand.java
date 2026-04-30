package fr.riege.ebsl.terminal.commands;

import fr.riege.ebsl.pathfinding.PathfindingManager;
import fr.riege.ebsl.pathfinding.settings.PathfinderSettings;
import fr.riege.ebsl.terminal.Command;
import fr.riege.ebsl.terminal.CommandContext;
import fr.riege.ebsl.terminal.CommandHandler;
import fr.riege.ebsl.terminal.CommandResult;
import fr.riege.ebsl.terminal.CommandScope;

@Command(name = "status", description = "Show navigation status", usage = "status", scope = CommandScope.MC)
public final class StatusCommand implements CommandHandler {

    @Override
    public CommandResult execute(CommandContext ctx) {
        String state = PathfindingManager.isNavigating() ? "navigating" : "idle";
        String debug = PathfinderSettings.instance().showDebug.value() ? "on" : "off";
        int jump = PathfinderSettings.instance().maxJumpHeight.value();
        return CommandResult.ok("state=" + state + " | jump=" + jump + " | debug=" + debug);
    }
}
