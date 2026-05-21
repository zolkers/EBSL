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

package fr.riege.ebsl.common.feature.terminal.goal;

import fr.riege.ebsl.common.pathfinding.goal.*;
import fr.riege.ebsl.common.platform.service.EbslServices;
import fr.riege.ebsl.common.platform.service.NavigationService;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class GoalUiCatalog {
    private static final List<GoalUiDefinition> GOALS = GoalCatalog.all().stream()
        .map(GoalUiCatalog::adapt)
        .toList();

    private static final Map<String, GoalUiDefinition> BY_ID = GOALS.stream()
        .collect(Collectors.toUnmodifiableMap(GoalUiDefinition::id, Function.identity()));

    private GoalUiCatalog() {}

    public static List<GoalUiDefinition> all() { return GOALS; }

    public static GoalUiDefinition byId(String id) {
        GoalUiDefinition def = BY_ID.get(id);
        if (def == null) throw new IllegalArgumentException("Unknown goal: " + id);
        return def;
    }

    private static int start(NavigationService nav, Goal goal, NavigationModeType mode) {
        nav.startNavigation(NavigationRequest.builder(goal).mode(mode).build());
        return 1;
    }

    private static GoalUiDefinition adapt(GoalDefinition definition) {
        GoalUiDefinition.Builder builder = GoalUiDefinition.builder(definition.id(), definition.label())
            .description(definition.description())
            .executor((nav, values) -> start(nav, definition.create(values, currentContext()), definition.mode()));
        for (GoalParameterSpec parameter : definition.parameters()) {
            builder.parameter(GoalParameter.from(parameter));
        }
        return builder.build();
    }

    private static GoalContext currentContext() {
        var pos = EbslServices.platform().player().position();
        return new GoalContext(
            (int) Math.floor(pos.x()),
            (int) Math.floor(pos.y()),
            (int) Math.floor(pos.z()));
    }
}
