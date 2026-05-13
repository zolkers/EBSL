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

package fr.riege.ebsl.common.feature.scripting.nodes;

import fr.riege.ebsl.common.core.settings.StringSetting;
import fr.riege.ebsl.common.feature.scripting.EbslNodeInvocation;
import fr.riege.ebsl.common.feature.scripting.annotations.EbslNodeDefinition;
import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeType;

@EbslNodeDefinition(EbslNodeType.OPERATOR_MOD)
public final class OperatorModNode extends AbstractEbslNode {
    @Override
    protected void registerSettings() {
        registerSetting(new StringSetting("variable", "Variable", "result"));
        registerSetting(new StringSetting("left", "Left", "0"));
        registerSetting(new StringSetting("right", "Right", "1"));
    }

    @Override
    public int start(EbslNodeInvocation invocation) {
        if (invocation.args().size() >= 3) {
            double right = invocation.runtime().number(invocation.runtime().value(invocation.arg(2)));
            double result = right == 0.0 ? 0.0 : invocation.runtime().number(invocation.runtime().value(invocation.arg(1))) % right;
            invocation.runtime().setVariable(invocation.arg(0), result);
        }
        return 0;
    }
}
