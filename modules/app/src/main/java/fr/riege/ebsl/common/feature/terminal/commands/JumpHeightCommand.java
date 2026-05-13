/*
 * This file is part of EBSL.
 *
 * EBSL is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EBSL is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package fr.riege.ebsl.common.feature.terminal.commands;

import fr.riege.ebsl.common.feature.terminal.*;
import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;

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
