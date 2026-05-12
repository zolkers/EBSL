package fr.riege.ebsl.common.feature.terminal.commands;

import fr.riege.ebsl.common.platform.service.EbslServices;
import fr.riege.ebsl.common.feature.terminal.*;

@Command(name = CommandIds.TEST_XZ, description = "Run A* XZ test (visualizer only)", usage = CommandIds.TEST_XZ + " <x> <z>", scope = CommandScope.MC)
public final class TestXzCommand implements CommandHandler {
    @Override
    public CommandResult execute(CommandContext ctx) {
        if (ctx.argCount() != 2) return CommandResult.badUsage(CommandIds.TEST_XZ + " <x> <z>");
        int x = ctx.argInt(0);
        int z = ctx.argInt(1);
        EbslServices.navigation().startPathTestXZ(x, z);
        return CommandResult.ok("Path test XZ to " + x + " " + z);
    }
}
