package fr.riege.ebsl.terminal.commands;

import fr.riege.ebsl.pathfinding.PathfindingManager;
import fr.riege.ebsl.pathfinding.goal.GoalBlock;
import fr.riege.ebsl.pathfinding.goal.NavigationModeType;
import fr.riege.ebsl.pathfinding.goal.NavigationRequest;
import fr.riege.ebsl.terminal.Command;
import fr.riege.ebsl.terminal.CommandContext;
import fr.riege.ebsl.terminal.CommandHandler;
import fr.riege.ebsl.terminal.CommandResult;
import fr.riege.ebsl.terminal.CommandScope;

@Command(name = "walk", description = "Walk to coordinates", usage = "walk <x> <y> <z>", scope = CommandScope.MC)
public final class WalkCommand implements CommandHandler {

    @Override
    public CommandResult execute(CommandContext ctx) {
        if (ctx.argCount() != 3) return CommandResult.badUsage("walk <x> <y> <z>");
        int x = ctx.argInt(0), y = ctx.argInt(1), z = ctx.argInt(2);
        PathfindingManager.startGoal(ctx.mc(), NavigationRequest.builder(new GoalBlock(x, y, z))
            .mode(NavigationModeType.WALK).build());
        return CommandResult.ok("Walking to " + x + " " + y + " " + z);
    }
}
