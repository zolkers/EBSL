package fr.riege.ebsl.common.feature.terminal.commands;

import fr.riege.ebsl.common.platform.service.EbslServices;
import fr.riege.ebsl.common.feature.terminal.*;

@Command(name = CommandIds.TEST, description = "Run A* test (visualizer only, no movement)", usage = CommandIds.TEST + " <x> <y> <z>", scope = CommandScope.MC)
public final class TestCommand implements CommandHandler {
    @Override
    public CommandResult execute(CommandContext ctx) {
        if (ctx.argCount() != 3) return CommandResult.badUsage(CommandIds.TEST + " <x> <y> <z>");
        int x = ctx.argInt(0);
        int y = ctx.argInt(1);
        int z = ctx.argInt(2);
        EbslServices.navigation().startPathTest(x, y, z);
        return CommandResult.ok("Path test to " + x + " " + y + " " + z);
    }
}
