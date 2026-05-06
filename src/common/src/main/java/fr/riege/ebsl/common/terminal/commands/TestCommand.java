package fr.riege.ebsl.common.terminal.commands;

import fr.riege.ebsl.common.service.EbslServices;
import fr.riege.ebsl.common.terminal.Command;
import fr.riege.ebsl.common.terminal.CommandContext;
import fr.riege.ebsl.common.terminal.CommandHandler;
import fr.riege.ebsl.common.terminal.CommandResult;
import fr.riege.ebsl.common.terminal.CommandScope;

@Command(name = "test", description = "Run A* test (visualizer only, no movement)", usage = "test <x> <y> <z>", scope = CommandScope.MC)
public final class TestCommand implements CommandHandler {
    @Override
    public CommandResult execute(CommandContext ctx) {
        if (ctx.argCount() != 3) return CommandResult.badUsage("test <x> <y> <z>");
        int x = ctx.argInt(0);
        int y = ctx.argInt(1);
        int z = ctx.argInt(2);
        EbslServices.navigation().startPathTest(x, y, z);
        return CommandResult.ok("Path test to " + x + " " + y + " " + z);
    }
}
