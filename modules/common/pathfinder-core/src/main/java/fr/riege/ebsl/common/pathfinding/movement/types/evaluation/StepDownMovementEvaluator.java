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

@MovementHandler(Node.MoveType.STEP_DOWN)
final class StepDownMovementEvaluator implements MovementEvaluator {
    @Override
    public MovementValidationResult validate(MovementValidationContext context) {
        int x = context.targetX();
        int y = context.targetY();
        int z = context.targetZ();
        if (!context.checker().world().isLoaded(x, y, z)
            || !context.checker().world().isLoaded(x, y + 1, z)) {
            return MovementValidationResult.invalid("step-down target is not loaded at " + x + ", " + y + ", " + z);
        }
        if (context.checker().world().isSolid(x, y + 1, z)) {
            return MovementValidationResult.invalid("step-down headroom blocked at " + x + ", " + (y + 1) + ", " + z);
        }
        if (context.navigationPointProvider()
            .getNavigationPoint(context.target().position, null)
            .isTraversable()) {
            return MovementValidationResult.ok();
        }
        return MovementValidationResult.invalid("step-down landing is no longer traversable at "
            + x + ", " + y + ", " + z);
    }

    @Override
    public boolean reducesSprintNearWaypoint() {
        return true;
    }

    @Override
    public boolean countsAsStairSequence() {
        return true;
    }
}
