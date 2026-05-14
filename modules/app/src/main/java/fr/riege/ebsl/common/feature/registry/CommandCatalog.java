/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Copyright (C) 2026 Victor Riegert and EBSL contributors
 *
 * This file is part of EBSL.
 *
 * EBSL is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EBSL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EBSL. If not, see <https://www.gnu.org/licenses/>.
 */

package fr.riege.ebsl.common.feature.registry;

import fr.riege.ebsl.common.feature.terminal.*;

import java.util.List;

public final class CommandCatalog {
    CommandCatalog() {
    }

    public void register(CommandHandler handler) {
        CommandRegistry.register(handler);
    }

    public void register(CommandSpec spec) {
        CommandRegistry.register(spec);
    }

    public CommandResult dispatch(String input) {
        return CommandRegistry.dispatch(input);
    }

    public List<CommandMeta> allMeta() {
        return CommandRegistry.allMeta();
    }

    public CommandHandler handler(String name) {
        return CommandRegistry.handler(name);
    }

    public List<CommandSuggestion> suggest(String input) {
        return CommandRegistry.suggest(input);
    }
}
