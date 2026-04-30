package fr.riege.ebsl.terminal.commands;

import fr.riege.ebsl.pathfinding.PathfindingManager;
import fr.riege.ebsl.pathfinding.goal.GoalNear;
import fr.riege.ebsl.pathfinding.goal.NavigationModeType;
import fr.riege.ebsl.pathfinding.goal.NavigationRequest;
import fr.riege.ebsl.terminal.Command;
import fr.riege.ebsl.terminal.CommandContext;
import fr.riege.ebsl.terminal.CommandHandler;
import fr.riege.ebsl.terminal.CommandResult;
import fr.riege.ebsl.terminal.CommandScope;

@Command(name = "near", description = "Walk near a position within radius", usage = "near <x> <y> <z> <radius>", scope = CommandScope.MC)
public final class NearCommand implements CommandHandler {

    @Override
    public CommandResult execute(CommandContext ctx) {
        if (ctx.argCount() != 4) return CommandResult.badUsage("near <x> <y> <z> <radius>");
        int x = ctx.argInt(0), y = ctx.argInt(1), z = ctx.argInt(2), r = ctx.argInt(3);
        if (r < 1 || r > 8) return CommandResult.error("Radius must be between 1 and 8.");
        PathfindingManager.startGoal(ctx.mc(), NavigationRequest.builder(new GoalNear(x, y, z, r))
            .mode(NavigationModeType.WALK).build());
        return CommandResult.ok("Walking near " + x + " " + y + " " + z + " (r=" + r + ")");
    }
}
