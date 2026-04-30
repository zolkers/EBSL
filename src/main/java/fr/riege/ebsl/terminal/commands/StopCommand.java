package fr.riege.ebsl.terminal.commands;

import fr.riege.ebsl.pathfinding.PathfindingManager;
import fr.riege.ebsl.terminal.Command;
import fr.riege.ebsl.terminal.CommandContext;
import fr.riege.ebsl.terminal.CommandHandler;
import fr.riege.ebsl.terminal.CommandResult;
import fr.riege.ebsl.terminal.CommandScope;

@Command(name = "stop", description = "Stop navigation", scope = CommandScope.BOTH)
public final class StopCommand implements CommandHandler {

    @Override
    public CommandResult execute(CommandContext ctx) {
        boolean was = PathfindingManager.isNavigating();
        PathfindingManager.stop(false);
        return CommandResult.ok(was ? "Navigation stopped." : "Not navigating.");
    }
}
