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
package fr.riege.ebsl.common.pathfinding.movement.types.evaluation;

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.movement.WalkabilityChecker;
import fr.riege.ebsl.common.pathfinding.provider.NavigationPointProvider;

public record MovementValidationContext(
    WalkabilityChecker checker,
    NavigationPointProvider navigationPointProvider,
    Node from,
    Node target,
    Node next,
    Vec3d playerPos,
    int pursuitSegment
) {
    public int targetX() {
        return target.position.flooredX();
    }

    public int targetY() {
        return target.position.flooredY();
    }

    public int targetZ() {
        return target.position.flooredZ();
    }
}
