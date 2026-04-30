package fr.riege.ebsl.terminal.commands;

import fr.riege.ebsl.pathfinding.PathfindingManager;
import fr.riege.ebsl.terminal.Command;
import fr.riege.ebsl.terminal.CommandContext;
import fr.riege.ebsl.terminal.CommandHandler;
import fr.riege.ebsl.terminal.CommandResult;
import fr.riege.ebsl.terminal.CommandScope;

@Command(name = "test", description = "Run A* test (visualizer only, no movement)", usage = "test <x> <y> <z>", scope = CommandScope.MC)
public final class TestCommand implements CommandHandler {

    @Override
    public CommandResult execute(CommandContext ctx) {
        if (ctx.argCount() != 3) return CommandResult.badUsage("test <x> <y> <z>");
        int x = ctx.argInt(0), y = ctx.argInt(1), z = ctx.argInt(2);
        PathfindingManager.startPathTest(ctx.mc(), x, y, z);
        return CommandResult.ok("Path test to " + x + " " + y + " " + z);
    }
}
