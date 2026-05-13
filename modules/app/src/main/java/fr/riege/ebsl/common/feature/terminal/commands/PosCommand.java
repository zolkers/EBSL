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
import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.platform.service.EbslServices;

@Command(name = CommandIds.POS, description = "Show player position", scope = CommandScope.MC)
public final class PosCommand implements CommandHandler {
    @Override
    public CommandResult execute(CommandContext ctx) {
        Vec3d pos = EbslServices.platform().player().position();
        return CommandResult.ok(String.format("%.2f  %.2f  %.2f", pos.x(), pos.y(), pos.z()));
    }
}
