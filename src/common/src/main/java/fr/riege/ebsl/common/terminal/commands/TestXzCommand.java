package fr.riege.ebsl.common.terminal.commands;

import fr.riege.ebsl.common.service.EbslServices;
import fr.riege.ebsl.common.terminal.Command;
import fr.riege.ebsl.common.terminal.CommandContext;
import fr.riege.ebsl.common.terminal.CommandHandler;
import fr.riege.ebsl.common.terminal.CommandResult;
import fr.riege.ebsl.common.terminal.CommandScope;

@Command(name = "testxz", description = "Run A* XZ test (visualizer only)", usage = "testxz <x> <z>", scope = CommandScope.MC)
public final class TestXzCommand implements CommandHandler {
    @Override
    public CommandResult execute(CommandContext ctx) {
        if (ctx.argCount() != 2) return CommandResult.badUsage("testxz <x> <z>");
        int x = ctx.argInt(0);
        int z = ctx.argInt(1);
        EbslServices.navigation().startPathTestXZ(x, z);
        return CommandResult.ok("Path test XZ to " + x + " " + z);
    }
}
