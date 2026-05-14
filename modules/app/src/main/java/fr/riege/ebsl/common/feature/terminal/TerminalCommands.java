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
import fr.riege.ebsl.common.feature.registry.FeatureRegistries;
import fr.riege.ebsl.common.feature.terminal.commands.*;

public final class TerminalCommands {
    private TerminalCommands() {
    }

    public static void bootstrap() {
        FeatureRegistries.commands().register(new HelpCommand());
        FeatureRegistries.commands().register(new StopCommand());
        FeatureRegistries.commands().register(new PosCommand());
        FeatureRegistries.commands().register(new StatusCommand());
        FeatureRegistries.commands().register(new DebugCommand());
        FeatureRegistries.commands().register(new JumpHeightCommand());
        FeatureRegistries.commands().register(new UiCommand());
        FeatureRegistries.commands().register(EbslCommand.spec());
        FeatureRegistries.commands().register(new TestCommand());
        FeatureRegistries.commands().register(new TestXzCommand());
        FeatureRegistries.commands().register(GoalCommand.spec());
    }
}
