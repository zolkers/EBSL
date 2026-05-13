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
package fr.riege.ebsl.common.pathfinding.movement.types.execution;

import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.movement.types.annotation.MovementHandler;

@MovementHandler(Node.MoveType.JUMP)
final class JumpMovementExecutor implements MovementExecutor {
    private static final double PARTIAL_SUPPORT_JUMP_TRIGGER_DISTANCE = 1.35;

    @Override
    public void handleJump(MovementExecutionContext context) {
        if (!context.canStartJump()) {
            return;
        }
        double triggerDistance = context.partialSupportAscent()
            ? Math.max(context.stepUpTriggerDistance(), PARTIAL_SUPPORT_JUMP_TRIGGER_DISTANCE)
            : context.jumpTriggerDistance();
        if (context.horizontalDistance() < triggerDistance) {
            context.pressJump();
        }
    }
}
