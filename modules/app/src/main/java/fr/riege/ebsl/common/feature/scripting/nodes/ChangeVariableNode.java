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

@EbslNodeDefinition(value = EbslNodeType.CHANGE_VARIABLE, aliases = {"change"})
public final class ChangeVariableNode extends AbstractEbslNode {
    @Override
    protected void registerSettings() {
        registerSetting(new StringSetting("variable", "Variable", "name"));
        registerSetting(new StringSetting("amount", "Amount", "1"));
    }

    @Override
    public int start(EbslNodeInvocation invocation) {
        if (invocation.args().size() >= 2) {
            double value = invocation.runtime().number(invocation.runtime().variable(invocation.arg(0)))
                + invocation.runtime().number(invocation.runtime().value(invocation.arg(1)));
            invocation.runtime().setVariable(invocation.arg(0), value);
        }
        return 0;
    }
}
