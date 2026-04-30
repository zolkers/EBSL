package fr.riege.ebsl.terminal.commands;

import fr.riege.ebsl.pathfinding.PathfindingManager;
import fr.riege.ebsl.terminal.Command;
import fr.riege.ebsl.terminal.CommandContext;
import fr.riege.ebsl.terminal.CommandHandler;
import fr.riege.ebsl.terminal.CommandResult;
import fr.riege.ebsl.terminal.CommandScope;

@Command(name = "testxz", description = "Run A* XZ test (visualizer only)", usage = "testxz <x> <z>", scope = CommandScope.MC)
public final class TestXzCommand implements CommandHandler {

    @Override
    public CommandResult execute(CommandContext ctx) {
        if (ctx.argCount() != 2) return CommandResult.badUsage("testxz <x> <z>");
        int x = ctx.argInt(0), z = ctx.argInt(1);
        PathfindingManager.startPathTestXZ(ctx.mc(), x, z);
        return CommandResult.ok("Path test XZ to " + x + " " + z);
    }
}
