package fr.riege.ebsl.common.feature.terminal.commands;

import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;
import fr.riege.ebsl.common.feature.terminal.*;

@Command(name = CommandIds.JUMP_HEIGHT, description = "Set max jump height (1-20)", usage = CommandIds.JUMP_HEIGHT + " <n>", scope = CommandScope.BOTH)
public final class JumpHeightCommand implements CommandHandler {
    @Override
    public CommandResult execute(CommandContext ctx) {
        if (ctx.argCount() != 1) return CommandResult.badUsage(CommandIds.JUMP_HEIGHT + " <n>");
        int n = ctx.argInt(0);
        if (n < 1 || n > 20) return CommandResult.error("Jump height must be 1-20");
        PathfinderSettings.instance().maxJumpHeight.setValue(n);
        return CommandResult.ok("Max jump height: " + n);
    }
}
