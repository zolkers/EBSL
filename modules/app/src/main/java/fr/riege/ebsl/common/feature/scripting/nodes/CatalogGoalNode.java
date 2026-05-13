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

import fr.riege.ebsl.common.core.settings.IntSetting;
import fr.riege.ebsl.common.feature.scripting.EbslNodeInvocation;
import fr.riege.ebsl.common.feature.terminal.goal.GoalParameter;
import fr.riege.ebsl.common.feature.terminal.goal.GoalUiDefinition;

import java.util.LinkedHashMap;
import java.util.Map;

public final class CatalogGoalNode extends NavigationNode {
    private static final String PREFIX = "goal_";
    private final GoalUiDefinition definition;

    public CatalogGoalNode(GoalUiDefinition definition) {
        super(PREFIX + definition.id());
        this.definition = definition;
    }

    @Override
    protected void registerSettings() {
        for (GoalParameter parameter : definition.parameters()) {
            registerSetting(new IntSetting(parameter.id(), parameter.label(), 0, -30000000, 30000000));
        }
    }

    @Override
    public int start(EbslNodeInvocation invocation) {
        Map<String, Integer> values = new LinkedHashMap<>();
        for (int i = 0; i < definition.parameters().size(); i++) {
            GoalParameter parameter = definition.parameters().get(i);
            values.put(parameter.id(), integerArg(invocation, i));
        }
        definition.execute(invocation.runtime().navigation(), values);
        return 0;
    }

    private int integerArg(EbslNodeInvocation invocation, int index) {
        if (index >= invocation.args().size()) {
            return 0;
        }
        return (int) Math.floor(invocation.runtime().number(invocation.runtime().value(invocation.arg(index))));
    }
}
