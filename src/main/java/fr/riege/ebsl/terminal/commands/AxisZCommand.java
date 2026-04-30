package fr.riege.ebsl.terminal.commands;

import fr.riege.ebsl.pathfinding.PathfindingManager;
import fr.riege.ebsl.pathfinding.goal.GoalAxisZ;
import fr.riege.ebsl.pathfinding.goal.NavigationModeType;
import fr.riege.ebsl.pathfinding.goal.NavigationRequest;
import fr.riege.ebsl.terminal.Command;
import fr.riege.ebsl.terminal.CommandContext;
import fr.riege.ebsl.terminal.CommandHandler;
import fr.riege.ebsl.terminal.CommandResult;
import fr.riege.ebsl.terminal.CommandScope;

@Command(name = "axisz", description = "Walk to a Z axis line", usage = "axisz <z>", scope = CommandScope.MC)
public final class AxisZCommand implements CommandHandler {

    @Override
    public CommandResult execute(CommandContext ctx) {
        if (ctx.argCount() != 1) return CommandResult.badUsage("axisz <z>");
        int z = ctx.argInt(0);
        PathfindingManager.startGoal(ctx.mc(), NavigationRequest.builder(new GoalAxisZ(z))
            .mode(NavigationModeType.WALK).build());
        return CommandResult.ok("Walking to Z axis " + z);
    }
}
