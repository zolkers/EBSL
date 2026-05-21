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

package fr.riege.ebsl.common.feature.terminal;

import fr.riege.ebsl.common.feature.terminal.goal.GoalParameter;

import java.util.List;
import java.util.Map;

/**
 * Handles one terminal command invocation.
 *
 * <p>Handlers receive a parsed command context and may provide parameters, completion rules, and nested subcommands.</p>
 */
public interface CommandHandler {
    /**
     * Executes the operation represented by this contract.
 *
     * @param ctx the command context
     * @return the value defined by this contract
     */
    CommandResult execute(CommandContext ctx);


    /**
     * Returns command parameters expected by this handler.
 *
     * @return the requested values
     */
    default List<GoalParameter> params() {
        return List.of();
    }


    /**
     * Returns the completion provider for this handler.
 *
     * @return the value defined by this contract
     */
    default CommandCompletion completer() {
        return CommandCompletion.EMPTY;
    }


    /**
     * Returns nested command handlers keyed by subcommand name.
 *
     * @return the value defined by this contract
     */
    default Map<String, CommandHandler> subcommands() {
        return Map.of();
    }
}
