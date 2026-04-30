package fr.riege.ebsl.terminal.commands;

import fr.riege.ebsl.pathfinding.PathfindingManager;
import fr.riege.ebsl.pathfinding.goal.GoalRectangleXZ;
import fr.riege.ebsl.pathfinding.goal.NavigationModeType;
import fr.riege.ebsl.pathfinding.goal.NavigationRequest;
import fr.riege.ebsl.terminal.Command;
import fr.riege.ebsl.terminal.CommandContext;
import fr.riege.ebsl.terminal.CommandHandler;
import fr.riege.ebsl.terminal.CommandResult;
import fr.riege.ebsl.terminal.CommandScope;

@Command(name = "rect", description = "Walk to a rectangle XZ area", usage = "rect <x1> <z1> <x2> <z2>", scope = CommandScope.MC)
public final class RectCommand implements CommandHandler {

    @Override
    public CommandResult execute(CommandContext ctx) {
        if (ctx.argCount() != 4) return CommandResult.badUsage("rect <x1> <z1> <x2> <z2>");
        int x1 = ctx.argInt(0), z1 = ctx.argInt(1);
        int x2 = ctx.argInt(2), z2 = ctx.argInt(3);
        int minX = Math.min(x1, x2), maxX = Math.max(x1, x2);
        int minZ = Math.min(z1, z2), maxZ = Math.max(z1, z2);
        PathfindingManager.startGoal(ctx.mc(), NavigationRequest.builder(new GoalRectangleXZ(minX, minZ, maxX, maxZ))
            .mode(NavigationModeType.WALK).build());
        return CommandResult.ok("Walking to rect [" + minX + "," + minZ + " -> " + maxX + "," + maxZ + "]");
    }
}
