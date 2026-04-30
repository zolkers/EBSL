package fr.riege.ebsl.terminal.commands;

import fr.riege.ebsl.pathfinding.PathfindingManager;
import fr.riege.ebsl.pathfinding.goal.GoalGetToBlock;
import fr.riege.ebsl.pathfinding.goal.NavigationModeType;
import fr.riege.ebsl.pathfinding.goal.NavigationRequest;
import fr.riege.ebsl.terminal.Command;
import fr.riege.ebsl.terminal.CommandContext;
import fr.riege.ebsl.terminal.CommandHandler;
import fr.riege.ebsl.terminal.CommandResult;
import fr.riege.ebsl.terminal.CommandScope;

@Command(name = "getto", description = "Walk to an adjacent face of a block", usage = "getto <x> <y> <z>", scope = CommandScope.MC)
public final class GetToCommand implements CommandHandler {

    @Override
    public CommandResult execute(CommandContext ctx) {
        if (ctx.argCount() != 3) return CommandResult.badUsage("getto <x> <y> <z>");
        int x = ctx.argInt(0), y = ctx.argInt(1), z = ctx.argInt(2);
        PathfindingManager.startGoal(ctx.mc(), NavigationRequest.builder(new GoalGetToBlock(x, y, z))
            .mode(NavigationModeType.WALK).build());
        return CommandResult.ok("Walking to face of " + x + " " + y + " " + z);
    }
}
