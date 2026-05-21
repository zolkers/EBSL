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
import fr.riege.ebsl.common.feature.scripting.EbslDuration;
import fr.riege.ebsl.common.feature.scripting.EbslNodeInvocation;
import fr.riege.ebsl.common.feature.scripting.enums.EbslInputKey;
import fr.riege.ebsl.common.feature.scripting.runtime.EbslScriptRuntime;

abstract class TimedInputNode extends AbstractEbslNode {
    private final EbslInputKey key;
    private final String fallbackDuration;

    TimedInputNode(EbslInputKey key, String fallbackDuration) {
        super();
        this.key = key;
        this.fallbackDuration = fallbackDuration;
    }

    @Override
    protected void registerSettings() {
        registerSetting(new StringSetting("duration", "Duration", fallbackDuration));
    }

    @Override
    public final boolean releasesGameplayKeys() {
        return true;
    }

    @Override
    public int start(EbslNodeInvocation invocation) {
        press(invocation.runtime(), key);
        return EbslDuration.ticks(invocation.args().isEmpty() ? fallbackDuration : invocation.arg(0));
    }

    static void press(EbslScriptRuntime runtime, EbslInputKey key) {
        key.set(runtime.platform().input(), true);
    }

    static void press(EbslScriptRuntime runtime, String token) {
        EbslInputKey key = EbslInputKey.byToken(token);
        if (key != null) {
            press(runtime, key);
        }
    }
}
