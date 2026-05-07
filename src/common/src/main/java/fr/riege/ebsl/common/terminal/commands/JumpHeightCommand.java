package fr.riege.ebsl.common.terminal.commands;

import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;
import fr.riege.ebsl.common.terminal.*;

@Command(name = "jumpheight", description = "Set max jump height (1-20)", usage = "jumpheight <n>", scope = CommandScope.BOTH)
public final class JumpHeightCommand implements CommandHandler {
    @Override
    public CommandResult execute(CommandContext ctx) {
        if (ctx.argCount() != 1) return CommandResult.badUsage("jumpheight <n>");
        int n = ctx.argInt(0);
        if (n < 1 || n > 20) return CommandResult.error("Jump height must be 1-20");
        PathfinderSettings.instance().maxJumpHeight.setValue(n);
        return CommandResult.ok("Max jump height: " + n);
    }
}
