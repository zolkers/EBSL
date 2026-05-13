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

@Command(name = CommandIds.DEBUG, description = "Toggle pathfinder debug overlay", usage = CommandIds.DEBUG, scope = CommandScope.BOTH)
public final class DebugCommand implements CommandHandler {
    @Override
    public CommandResult execute(CommandContext ctx) {
        boolean next = !PathfinderSettings.instance().showDebug.value();
        PathfinderSettings.instance().showDebug.setValue(next);
        return CommandResult.ok("Debug: " + (next ? "on" : "off"));
    }
}
