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

package fr.riege.ebsl.tools.pathfindersim.ui;

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.pathfinding.goal.GoalCatalog;
import fr.riege.ebsl.common.pathfinding.goal.GoalContext;
import fr.riege.ebsl.common.pathfinding.goal.GoalDefinition;
import fr.riege.ebsl.common.pathfinding.goal.GoalParameterSpec;
import fr.riege.ebsl.common.pathfinding.goal.NavigationModeType;
import fr.riege.ebsl.common.pathfinding.goal.NavigationTarget;
import fr.riege.ebsl.tools.pathfindersim.world.minecraft.MinecraftWorldImportOptions;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

final class SimulationGoalInput {
    private final GoalDefinition definition;

    private SimulationGoalInput(GoalDefinition definition) {
        this.definition = definition;
    }

    static SimulationGoalInput[] values() {
        return GoalCatalog.all().stream()
            .filter(definition -> definition.mode() == NavigationModeType.WALK)
            .map(SimulationGoalInput::new)
            .toArray(SimulationGoalInput[]::new);
    }

    static SimulationGoalInput fallback() {
        return Arrays.stream(values())
            .filter(input -> GoalCatalog.WALK.equals(input.definition.id()))
            .findFirst()
            .orElseGet(() -> values()[0]);
    }

    String hint() {
        return definition.parameters().stream()
            .map(GoalParameterSpec::id)
            .reduce((left, right) -> left + ',' + right)
            .orElse("");
    }

    MinecraftWorldImportOptions toOptions(String input,
                                          Vec3d start,
                                          Path worldDirectory,
                                          MinecraftWorldImportOptions baseOptions) {
        int startY = floor(start.y());
        GoalContext context = new GoalContext(floor(start.x()), startY, floor(start.z()));
        NavigationTarget target = definition.create(values(input), context).resolve(context.x(), context.y(), context.z());
        int[] block = switchTarget(target, startY);
        return new MinecraftWorldImportOptions(
            worldDirectory,
            start,
            true,
            block[0],
            block[1],
            block[2],
            true,
            baseOptions.radiusChunks(),
            baseOptions.goalSearchBlocks(),
            baseOptions.diagnostics());
    }

    private Map<String, Integer> values(String input) {
        String[] parts = input.split(",");
        if (parts.length != definition.parameters().size()) {
            throw new IllegalArgumentException("Expected goal input: " + hint());
        }
        Map<String, Integer> parsed = new LinkedHashMap<>();
        for (int i = 0; i < definition.parameters().size(); i++) {
            parsed.put(definition.parameters().get(i).id(), integer(parts[i], definition.parameters().get(i).id()));
        }
        return parsed;
    }

    private static int[] switchTarget(NavigationTarget target, int fallbackY) {
        if (target instanceof NavigationTarget.Block block) {
            return new int[] { block.x(), block.y(), block.z() };
        }
        if (target instanceof NavigationTarget.Column column) {
            return new int[] { column.x(), fallbackY, column.z() };
        }
        throw new IllegalArgumentException("Unsupported navigation target: " + target);
    }

    private static int integer(String value, String parameter) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid integer for " + parameter + ": " + value, exception);
        }
    }

    private static int floor(double value) {
        return (int) Math.floor(value);
    }

    @Override
    public String toString() {
        return definition.label();
    }
}
