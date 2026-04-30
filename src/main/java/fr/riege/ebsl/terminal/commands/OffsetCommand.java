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

@Command(name = "offset", description = "Walk to a position relative to the player", usage = "offset <dx> <dy> <dz>", scope = CommandScope.MC)
public final class OffsetCommand implements CommandHandler {

    @Override
    public CommandResult execute(CommandContext ctx) {
        if (ctx.argCount() != 3) return CommandResult.badUsage("offset <dx> <dy> <dz>");
        int dx = ctx.argInt(0), dy = ctx.argInt(1), dz = ctx.argInt(2);
        int px = (int) Math.floor(ctx.mc().player.getX());
        int py = (int) Math.floor(ctx.mc().player.getY());
        int pz = (int) Math.floor(ctx.mc().player.getZ());
        int x = px + dx, y = py + dy, z = pz + dz;
        PathfindingManager.startGoal(ctx.mc(), NavigationRequest.builder(new GoalBlock(x, y, z))
            .mode(NavigationModeType.WALK).build());
        return CommandResult.ok("Walking to " + x + " " + y + " " + z + " (offset " + dx + " " + dy + " " + dz + ")");
    }
}
