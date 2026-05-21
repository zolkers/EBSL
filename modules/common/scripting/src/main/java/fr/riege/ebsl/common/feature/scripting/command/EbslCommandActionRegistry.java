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

package fr.riege.ebsl.common.feature.scripting.command;

import fr.riege.ebsl.common.core.registry.IRegistry;
import fr.riege.ebsl.common.core.registry.MapRegistry;
import fr.riege.ebsl.common.feature.terminal.CommandContext;
import fr.riege.ebsl.common.feature.terminal.CommandResult;
import java.util.Locale;

final class EbslCommandActionRegistry {
    private static final IRegistry<String, EbslCommandActionHandler> HANDLERS = new MapRegistry<>(null);

    static {
        register(EbslCommandAction.RUN, EbslCommand::runFile);
        register(EbslCommandAction.INLINE, EbslCommand::runInline);
        register(EbslCommandAction.STOP, EbslCommand::stop);
        register(EbslCommandAction.STATUS, EbslCommand::status);
        register(EbslCommandAction.TASKS, context -> CommandResult.ok(EbslCommand.taskLines()));
    }

    private EbslCommandActionRegistry() {
    }

    static CommandResult execute(String action, CommandContext context) {
        EbslCommandActionHandler handler = HANDLERS.get(normalize(action));
        return handler == null ? CommandResult.badUsage(EbslCommand.USAGE) : handler.execute(context);
    }

    private static void register(EbslCommandAction action, EbslCommandActionHandler handler) {
        HANDLERS.register(normalize(action.id()), handler);
    }

    private static String normalize(String token) {
        return token == null ? "" : token.trim().toLowerCase(Locale.ROOT).replace('-', '_');
    }
}
