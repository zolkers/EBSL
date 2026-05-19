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

package fr.riege.ebsl.common.pathfinding.goal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class GoalDefinition {
    private final String id;
    private final String label;
    private final String description;
    private final List<GoalParameterSpec> parameters;
    private final NavigationModeType mode;
    private final GoalFactory factory;

    private GoalDefinition(Builder builder) {
        id = builder.id;
        label = builder.label;
        description = builder.description;
        parameters = List.copyOf(builder.parameters);
        mode = builder.mode;
        factory = builder.factory;
    }

    public String id() {
        return id;
    }

    public String label() {
        return label;
    }

    public String description() {
        return description;
    }

    public List<GoalParameterSpec> parameters() {
        return parameters;
    }

    public NavigationModeType mode() {
        return mode;
    }

    public Goal create(Map<String, Integer> values, GoalContext context) {
        return factory.create(Collections.unmodifiableMap(new LinkedHashMap<>(values)), context);
    }

    public static Builder builder(String id, String label) {
        return new Builder(id, label);
    }

    public static final class Builder {
        private final String id;
        private final String label;
        private final List<GoalParameterSpec> parameters = new ArrayList<>();
        private String description = "";
        private NavigationModeType mode = NavigationModeType.WALK;
        private GoalFactory factory;

        private Builder(String id, String label) {
            this.id = id;
            this.label = label;
        }

        public Builder description(String value) {
            description = value;
            return this;
        }

        public Builder parameter(GoalParameterSpec value) {
            parameters.add(value);
            return this;
        }

        public Builder currentXYZ() {
            return parameter(GoalParameterSpec.currentX())
                .parameter(GoalParameterSpec.currentY())
                .parameter(GoalParameterSpec.currentZ());
        }

        public Builder mode(NavigationModeType value) {
            mode = value;
            return this;
        }

        public Builder factory(GoalFactory value) {
            factory = value;
            return this;
        }

        public GoalDefinition build() {
            if (factory == null) {
                throw new IllegalStateException("Goal " + id + " has no factory");
            }
            return new GoalDefinition(this);
        }
    }
}
