package fr.riege.ebsl.terminal.commands;

import fr.riege.ebsl.pathfinding.PathfindingManager;
import fr.riege.ebsl.pathfinding.goal.GoalYLevel;
import fr.riege.ebsl.pathfinding.goal.NavigationModeType;
import fr.riege.ebsl.pathfinding.goal.NavigationRequest;
import fr.riege.ebsl.terminal.Command;
import fr.riege.ebsl.terminal.CommandContext;
import fr.riege.ebsl.terminal.CommandHandler;
import fr.riege.ebsl.terminal.CommandResult;
import fr.riege.ebsl.terminal.CommandScope;

@Command(name = "y", description = "Walk to a Y level", usage = "y <targetY>", scope = CommandScope.MC)
public final class YCommand implements CommandHandler {

    @Override
    public CommandResult execute(CommandContext ctx) {
        if (ctx.argCount() != 1) return CommandResult.badUsage("y <targetY>");
        int targetY = ctx.argInt(0);
        PathfindingManager.startGoal(ctx.mc(), NavigationRequest.builder(new GoalYLevel(targetY))
            .mode(NavigationModeType.WALK).build());
        return CommandResult.ok("Walking to Y=" + targetY);
    }
}
