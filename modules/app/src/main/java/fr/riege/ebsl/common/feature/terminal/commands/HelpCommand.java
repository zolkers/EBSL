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

import java.util.ArrayList;
import java.util.List;

@Command(name = CommandIds.HELP, description = "List available commands", scope = CommandScope.TERMINAL)
public final class HelpCommand implements CommandHandler {
    @Override
    public CommandResult execute(CommandContext ctx) {
        List<String> lines = new ArrayList<>();
        for (CommandMeta meta : CommandRegistry.allMeta()) {
            String args = meta.usage().startsWith(meta.name())
                ? meta.usage().substring(meta.name().length()).trim()
                : meta.usage();
            lines.add(meta.name() + (args.isEmpty() ? "" : " " + args) + " - " + meta.description());
        }
        return CommandResult.ok(lines);
    }
}
