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
package fr.riege.ebsl.common.pathfinding.execution;

import fr.riege.ebsl.common.core.registry.EnumRegistry;
import fr.riege.ebsl.common.pathfinding.Node;

import java.util.List;
import java.util.Optional;

final class MovementSmoothingRegistry {
    private static final EnumRegistry<Node.MoveType, MovementSmoothing> STRATEGIES =
        new EnumRegistry<>(Node.MoveType.class, null);

    private MovementSmoothingRegistry() {
    }

    static Optional<MovementSmoothing.Plan> resolve(List<Node> path, int pursuitSegment, boolean alreadyRotating) {
        if (path == null || path.isEmpty()) {
            return Optional.empty();
        }

        int start = Math.clamp(pursuitSegment, 0, path.size() - 1);
        int end = (int) Math.clamp(start + 2L, 0L, path.size() - 1L);
        for (int i = start; i <= end; i++) {
            MovementSmoothing strategy = STRATEGIES.get(path.get(i).moveType());
            if (strategy != null && strategy.applies(path, pursuitSegment)) {
                return Optional.of(strategy.plan(path, pursuitSegment, alreadyRotating));
            }
        }
        return Optional.empty();
    }

}
