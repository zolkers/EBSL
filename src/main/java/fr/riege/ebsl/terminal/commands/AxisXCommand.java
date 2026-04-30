package fr.riege.ebsl.terminal.commands;

import fr.riege.ebsl.pathfinding.PathfindingManager;
import fr.riege.ebsl.pathfinding.goal.GoalAxisX;
import fr.riege.ebsl.pathfinding.goal.NavigationModeType;
import fr.riege.ebsl.pathfinding.goal.NavigationRequest;
import fr.riege.ebsl.terminal.Command;
import fr.riege.ebsl.terminal.CommandContext;
import fr.riege.ebsl.terminal.CommandHandler;
import fr.riege.ebsl.terminal.CommandResult;
import fr.riege.ebsl.terminal.CommandScope;

@Command(name = "axisx", description = "Walk to an X axis line", usage = "axisx <x>", scope = CommandScope.MC)
public final class AxisXCommand implements CommandHandler {

    @Override
    public CommandResult execute(CommandContext ctx) {
        if (ctx.argCount() != 1) return CommandResult.badUsage("axisx <x>");
        int x = ctx.argInt(0);
        PathfindingManager.startGoal(ctx.mc(), NavigationRequest.builder(new GoalAxisX(x))
            .mode(NavigationModeType.WALK).build());
        return CommandResult.ok("Walking to X axis " + x);
    }
}
