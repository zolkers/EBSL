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
import fr.riege.ebsl.common.platform.service.EbslServices;

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
