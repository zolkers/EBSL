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
package fr.riege.ebsl.common.pathfinding.check;

import fr.riege.ebsl.common.core.registry.EnumRegistry;
import fr.riege.ebsl.common.pathfinding.Node;

import java.util.List;

final class MovementPathCheckRegistry {
    private static final EnumRegistry<Node.MoveType, List<PathCheck>> CHECKS =
        new EnumRegistry<>(Node.MoveType.class, List.of());

    static {
        register(Node.MoveType.PARKOUR, List.of());
    }

    private MovementPathCheckRegistry() {
    }

    static PathCheckResult evaluate(PathCheckContext context) {
        for (PathCheck check : CHECKS.get(context.currentMoveType())) {
            PathCheckResult result = check.evaluate(context);
            if (result.requiresAction()) {
                return result;
            }
        }
        if (context.currentMoveType() != Node.MoveType.PARKOUR
            && context.hasMoveTypeInWindow(Node.MoveType.PARKOUR, 2)) {
            for (PathCheck check : CHECKS.get(Node.MoveType.PARKOUR)) {
                PathCheckResult result = check.evaluate(context);
                if (result.requiresAction()) {
                    return result;
                }
            }
        }
        return PathCheckResult.none();
    }

    private static void register(Node.MoveType moveType, List<PathCheck> checks) {
        CHECKS.register(moveType, List.copyOf(checks));
    }
}
