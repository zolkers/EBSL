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

import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.movement.types.annotation.MovementHandler;
@MovementHandler(Node.MoveType.FALL)
final class FallMovementEvaluator extends WalkMovementEvaluator {
    @Override
    public MovementValidationResult validate(MovementValidationContext context) {
        int fromY = context.from().position.flooredY();
        int x = context.targetX();
        int y = context.targetY();
        int z = context.targetZ();
        if (!context.checker().safeToFall(fromY, x, y, z)) {
            return MovementValidationResult.invalid("fall segment became unsafe at " + x + ", " + y + ", " + z);
        }
        if (context.navigationPointProvider()
            .getNavigationPoint(context.target().position, null)
            .isTraversable()) {
            return MovementValidationResult.ok();
        }
        return MovementValidationResult.invalid("fall landing is no longer traversable at "
            + x + ", " + y + ", " + z);
    }
}
