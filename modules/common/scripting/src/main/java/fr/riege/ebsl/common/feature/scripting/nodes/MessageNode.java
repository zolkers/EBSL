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

package fr.riege.ebsl.common.feature.scripting.nodes;

import fr.riege.ebsl.common.core.settings.StringSetting;
import fr.riege.ebsl.common.feature.scripting.EbslNodeInvocation;
import fr.riege.ebsl.common.feature.scripting.annotations.EbslNodeDefinition;
import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeType;

import java.util.List;

@EbslNodeDefinition(EbslNodeType.MESSAGE)
public final class MessageNode extends AbstractEbslNode {
    private StringSetting text;

    @Override
    protected void registerSettings() {
        text = registerSetting(new StringSetting("text", "Text", "hello"));
    }

    @Override
    public int start(EbslNodeInvocation invocation) {
        invocation.runtime().platform().commands().printSuccess(String.join(" ", invocation.args()));
        return 0;
    }

    @Override
    public void loadArgs(List<String> args) {
        settings();
        text.setValue(String.join(" ", args));
    }
}
