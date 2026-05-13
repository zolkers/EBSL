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

import fr.riege.ebsl.common.pathfinding.annotation.PathCheckRole;
import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;

@PathCheckRole("huge_deviation_replan")
final class HugeDeviationCheck implements PathCheck {
    @Override
    public PathCheckResult evaluate(PathCheckContext context) {
        PathProximitySnapshot proximity = context.proximity();
        if (proximity.horizontalDistance() >= PathfinderSettings.instance().hugeDeviationHorizontalDistance.value()) {
            return PathCheckResult.repairToSegment(proximity.nearestSegmentIndex(), String.format(
                "huge horizontal deviation repair h=%.2f y=%.2f segment=%d",
                proximity.horizontalDistance(),
                proximity.verticalDistance(),
                proximity.nearestSegmentIndex()));
        }
        return PathCheckResult.none();
    }
}
