package fr.riege.ebsl.terminal.commands;

import fr.riege.ebsl.pathfinding.PathfindingManager;
import fr.riege.ebsl.pathfinding.goal.GoalChunk;
import fr.riege.ebsl.pathfinding.goal.NavigationModeType;
import fr.riege.ebsl.pathfinding.goal.NavigationRequest;
import fr.riege.ebsl.terminal.Command;
import fr.riege.ebsl.terminal.CommandContext;
import fr.riege.ebsl.terminal.CommandHandler;
import fr.riege.ebsl.terminal.CommandResult;
import fr.riege.ebsl.terminal.CommandScope;

@Command(name = "chunk", description = "Walk to a chunk", usage = "chunk <chunkX> <chunkZ>", scope = CommandScope.MC)
public final class ChunkCommand implements CommandHandler {

    @Override
    public CommandResult execute(CommandContext ctx) {
        if (ctx.argCount() != 2) return CommandResult.badUsage("chunk <chunkX> <chunkZ>");
        int cx = ctx.argInt(0), cz = ctx.argInt(1);
        PathfindingManager.startGoal(ctx.mc(), NavigationRequest.builder(new GoalChunk(cx, cz))
            .mode(NavigationModeType.WALK).build());
        return CommandResult.ok("Walking to chunk " + cx + " " + cz);
    }
}
