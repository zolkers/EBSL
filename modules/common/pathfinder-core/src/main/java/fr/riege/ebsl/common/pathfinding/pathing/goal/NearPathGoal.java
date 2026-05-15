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

package fr.riege.ebsl.common.pathfinding.pathing.goal;

import fr.riege.ebsl.common.pathfinding.pathing.heuristic.HeuristicContext;
import fr.riege.ebsl.common.pathfinding.pathing.heuristic.HeuristicWeights;
import fr.riege.ebsl.common.pathfinding.pathing.heuristic.IHeuristicStrategy;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;

import java.util.Objects;

/**
 * Goal satisfied by any position within a block-space radius of a center.
 */
public record NearPathGoal(PathPosition representative, int radius) implements PathGoal {
    public NearPathGoal {
        Objects.requireNonNull(representative, "representative");
        radius = Math.max(0, radius);
    }

    @Override
    public boolean isSatisfiedBy(PathPosition position) {
        int dx = Math.abs(position.flooredX() - representative.flooredX());
        int dy = Math.abs(position.flooredY() - representative.flooredY());
        int dz = Math.abs(position.flooredZ() - representative.flooredZ());
        return Math.max(dx, Math.max(dy, dz)) <= radius;
    }

    @Override
    public double estimate(PathPosition position, PathPosition start, HeuristicWeights weights, IHeuristicStrategy strategy) {
        double exact = strategy.calculate(new HeuristicContext(position, start, representative, weights));
        return Math.max(0.0, exact - radius);
    }
}
