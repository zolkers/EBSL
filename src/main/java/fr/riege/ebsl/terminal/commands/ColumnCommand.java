package fr.riege.ebsl.terminal.commands;

import fr.riege.ebsl.pathfinding.PathfindingManager;
import fr.riege.ebsl.pathfinding.goal.GoalColumn;
import fr.riege.ebsl.pathfinding.goal.NavigationModeType;
import fr.riege.ebsl.pathfinding.goal.NavigationRequest;
import fr.riege.ebsl.terminal.Command;
import fr.riege.ebsl.terminal.CommandContext;
import fr.riege.ebsl.terminal.CommandHandler;
import fr.riege.ebsl.terminal.CommandResult;
import fr.riege.ebsl.terminal.CommandScope;

@Command(name = "column", description = "Walk to a column (x z [radius])", usage = "column <x> <z> [radius]", scope = CommandScope.MC)
public final class ColumnCommand implements CommandHandler {

    @Override
    public CommandResult execute(CommandContext ctx) {
        int argc = ctx.argCount();
        if (argc < 2 || argc > 3) return CommandResult.badUsage("column <x> <z> [radius]");
        int x = ctx.argInt(0);
        int z = ctx.argInt(1);
        double radius = argc == 3 ? ctx.argDouble(2) : 0.5;
        PathfindingManager.startGoal(ctx.mc(), NavigationRequest.builder(new GoalColumn(x, z, radius))
            .mode(NavigationModeType.WALK).build());
        return CommandResult.ok("Walking to column " + x + " " + z + " r=" + radius);
    }
}
