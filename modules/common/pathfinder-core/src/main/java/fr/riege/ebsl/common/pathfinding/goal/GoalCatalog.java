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

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class GoalCatalog {
    public static final String WALK = "walk";
    private static final String RADIUS_PARAM = "radius";

    private static final List<GoalDefinition> GOALS = List.of(
        GoalDefinition.builder(WALK, "Walk")
            .description("Walk to an exact block.")
            .currentXYZ()
            .factory((v, context) -> new GoalBlock(v.get("x"), v.get("y"), v.get("z")))
            .build(),
        GoalDefinition.builder("fly", "Fly")
            .description("Fly to an exact block.")
            .currentXYZ()
            .mode(NavigationModeType.FLY)
            .factory((v, context) -> new GoalBlock(v.get("x"), v.get("y"), v.get("z")))
            .build(),
        GoalDefinition.builder("walkxz", "Walk XZ")
            .description("Walk to an X/Z column with long range support.")
            .parameter(GoalParameterSpec.currentX())
            .parameter(GoalParameterSpec.currentZ())
            .factory((v, context) -> new GoalXZ(v.get("x"), v.get("z")))
            .build(),
        GoalDefinition.builder("near", "Near")
            .description("Walk near a block within a radius.")
            .currentXYZ()
            .parameter(GoalParameterSpec.constant(RADIUS_PARAM, "Radius", 2))
            .factory((v, context) -> new GoalNear(v.get("x"), v.get("y"), v.get("z"), v.get(RADIUS_PARAM)))
            .build(),
        GoalDefinition.builder("getto", "Get To Block")
            .description("Get to a reachable adjacent position for a block.")
            .currentXYZ()
            .factory((v, context) -> new GoalGetToBlock(v.get("x"), v.get("y"), v.get("z")))
            .build(),
        GoalDefinition.builder("y", "Y Level")
            .description("Walk until the target Y level is reached.")
            .parameter(GoalParameterSpec.currentY())
            .factory((v, context) -> new GoalYLevel(v.get("y")))
            .build(),
        GoalDefinition.builder("offset", "Offset")
            .description("Walk relative to the current position.")
            .parameter(GoalParameterSpec.constant("dx", "dX", 0))
            .parameter(GoalParameterSpec.constant("dy", "dY", 0))
            .parameter(GoalParameterSpec.constant("dz", "dZ", 0))
            .factory(GoalCatalog::offset)
            .build(),
        GoalDefinition.builder("axisx", "Axis X")
            .description("Reach a target X axis.")
            .parameter(GoalParameterSpec.currentX())
            .factory((v, context) -> new GoalAxisX(v.get("x")))
            .build(),
        GoalDefinition.builder("axisz", "Axis Z")
            .description("Reach a target Z axis.")
            .parameter(GoalParameterSpec.currentZ())
            .factory((v, context) -> new GoalAxisZ(v.get("z")))
            .build(),
        GoalDefinition.builder("column", "Column")
            .description("Reach an X/Z column.")
            .parameter(GoalParameterSpec.currentX())
            .parameter(GoalParameterSpec.currentZ())
            .parameter(GoalParameterSpec.constant(RADIUS_PARAM, "Radius", 1))
            .factory((v, context) -> new GoalColumn(v.get("x"), v.get("z"), v.get(RADIUS_PARAM)))
            .build(),
        GoalDefinition.builder("rect", "Rectangle")
            .description("Reach any block inside an X/Z rectangle.")
            .parameter(GoalParameterSpec.currentX())
            .parameter(GoalParameterSpec.currentZ())
            .parameter(GoalParameterSpec.constant("x2", "X2", 0))
            .parameter(GoalParameterSpec.constant("z2", "Z2", 0))
            .factory(GoalCatalog::rectangle)
            .build(),
        GoalDefinition.builder("chunk", "Chunk")
            .description("Reach a chunk.")
            .parameter(GoalParameterSpec.constant("chunkX", "Chunk X", 0))
            .parameter(GoalParameterSpec.constant("chunkZ", "Chunk Z", 0))
            .factory((v, context) -> new GoalChunk(v.get("chunkX"), v.get("chunkZ")))
            .build()
    );

    private static final Map<String, GoalDefinition> BY_ID = GOALS.stream()
        .collect(Collectors.toUnmodifiableMap(GoalDefinition::id, Function.identity()));

    private GoalCatalog() {
    }

    public static List<GoalDefinition> all() {
        return GOALS;
    }

    public static GoalDefinition byId(String id) {
        GoalDefinition definition = BY_ID.get(id);
        if (definition == null) {
            throw new IllegalArgumentException("Unknown goal: " + id);
        }
        return definition;
    }

    private static Goal offset(Map<String, Integer> values, GoalContext context) {
        return new GoalBlock(
            context.x() + values.getOrDefault("dx", 0),
            context.y() + values.getOrDefault("dy", 0),
            context.z() + values.getOrDefault("dz", 0));
    }

    private static Goal rectangle(Map<String, Integer> values, GoalContext context) {
        return new GoalRectangleXZ(
            Math.min(values.get("x"), values.get("x2")),
            Math.min(values.get("z"), values.get("z2")),
            Math.max(values.get("x"), values.get("x2")),
            Math.max(values.get("z"), values.get("z2")));
    }
}
