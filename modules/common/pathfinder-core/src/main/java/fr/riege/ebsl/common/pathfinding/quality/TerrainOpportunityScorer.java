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
package fr.riege.ebsl.common.pathfinding.quality;

import fr.riege.ebsl.common.pathfinding.movement.MovementTerrain;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;

public final class TerrainOpportunityScorer {
    private TerrainOpportunityScorer() {
    }

    public static double scorePosition(MovementTerrain checker, PathPosition position) {
        if (checker == null || position == null) {
            return 0.0;
        }
        int x = position.flooredX();
        int y = position.flooredY();
        int z = position.flooredZ();
        if (!checker.world().isLoaded(x, y, z) || checker.isDangerous(x, y, z) || checker.isDangerous(x, y + 1, z)) {
            return 0.0;
        }
        double score = 0.0;
        score += checker.isWalkable(x, y, z) ? 0.38 : 0.0;
        score += checker.isPassable(x, y, z) && checker.isPassable(x, y + 1, z) ? 0.18 : 0.0;
        score += checker.hasWalkableTop(x, y - 1, z) || checker.isLowPartialSupport(x, y, z) ? 0.20 : 0.0;
        score += openness(checker, x, y, z) * 0.16;
        score += supportShape(checker, x, y, z) * 0.08;
        if (checker.isWater(x, y, z)) {
            score -= 0.12;
        }
        return Math.clamp(score, 0.0, 1.0);
    }

    private static double openness(MovementTerrain checker, int x, int y, int z) {
        int blocked = 0;
        blocked += checker.isFullWall(x + 1, y, z) ? 1 : 0;
        blocked += checker.isFullWall(x - 1, y, z) ? 1 : 0;
        blocked += checker.isFullWall(x, y, z + 1) ? 1 : 0;
        blocked += checker.isFullWall(x, y, z - 1) ? 1 : 0;
        return 1.0 - blocked / 4.0;
    }

    private static double supportShape(MovementTerrain checker, int x, int y, int z) {
        double top = checker.getTopY(x, y - 1, z);
        if (top >= 0.95) {
            return 1.0;
        }
        if (top >= 0.5) {
            return 0.72;
        }
        return checker.isLowPartialSupport(x, y, z) ? 0.58 : 0.0;
    }
}
