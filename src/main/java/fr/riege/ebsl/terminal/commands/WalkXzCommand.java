package fr.riege.ebsl.terminal.commands;

import fr.riege.ebsl.pathfinding.PathfindingManager;
import fr.riege.ebsl.pathfinding.goal.GoalXZ;
import fr.riege.ebsl.pathfinding.goal.NavigationModeType;
import fr.riege.ebsl.pathfinding.goal.NavigationRequest;
import fr.riege.ebsl.terminal.Command;
import fr.riege.ebsl.terminal.CommandContext;
import fr.riege.ebsl.terminal.CommandHandler;
import fr.riege.ebsl.terminal.CommandResult;
import fr.riege.ebsl.terminal.CommandScope;

@Command(name = "walkxz", description = "Walk to XZ position (any Y)", usage = "walkxz <x> <z>", scope = CommandScope.MC)
public final class WalkXzCommand implements CommandHandler {

    @Override
    public CommandResult execute(CommandContext ctx) {
        if (ctx.argCount() != 2) return CommandResult.badUsage("walkxz <x> <z>");
        int x = ctx.argInt(0), z = ctx.argInt(1);
        PathfindingManager.startGoal(ctx.mc(), NavigationRequest.builder(new GoalXZ(x, z))
            .mode(NavigationModeType.WALK).build());
        return CommandResult.ok("Walking to XZ " + x + " " + z);
    }
}
