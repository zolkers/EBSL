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

import fr.riege.ebsl.common.feature.scripting.command.EbslCommand;
import fr.riege.ebsl.common.feature.terminal.commands.*;

public final class TerminalCommands {
    private TerminalCommands() {
    }

    public static void bootstrap() {
        CommandRegistry.register(new HelpCommand());
        CommandRegistry.register(new StopCommand());
        CommandRegistry.register(new PosCommand());
        CommandRegistry.register(new StatusCommand());
        CommandRegistry.register(new DebugCommand());
        CommandRegistry.register(new JumpHeightCommand());
        CommandRegistry.register(new UiCommand());
        CommandRegistry.register(EbslCommand.spec());
        CommandRegistry.register(new TestCommand());
        CommandRegistry.register(new TestXzCommand());
        CommandRegistry.register(GoalCommand.spec());
    }
}
