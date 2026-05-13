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

package fr.riege.ebsl.common.pathfinding.movement.types.evaluation;

import fr.riege.ebsl.common.core.registry.EnumRegistry;
import fr.riege.ebsl.common.pathfinding.Node;

public final class MovementEvaluatorRegistry {
    private static final EnumRegistry<Node.MoveType, MovementEvaluator> EVALUATORS =
        new EnumRegistry<>(Node.MoveType.class, new WalkMovementEvaluator());

    static {
        register(Node.MoveType.WALK, new WalkMovementEvaluator());
        register(Node.MoveType.WALK_DIAGONAL, new WalkDiagonalMovementEvaluator());
        register(Node.MoveType.STEP_UP, new StepUpMovementEvaluator());
        register(Node.MoveType.STEP_DOWN, new StepDownMovementEvaluator());
        register(Node.MoveType.JUMP, new JumpMovementEvaluator());
        register(Node.MoveType.PARKOUR, new ParkourMovementEvaluator());
        register(Node.MoveType.FALL, new FallMovementEvaluator());
        register(Node.MoveType.SWIM, new SwimMovementEvaluator());
        register(Node.MoveType.CLIMB, new ClimbMovementEvaluator());
        register(Node.MoveType.FLY, new FlyMovementEvaluator());
        ensureComplete();
    }

    private MovementEvaluatorRegistry() {
    }

    public static MovementEvaluator get(Node.MoveType type) {
        return EVALUATORS.get(type);
    }

    private static void register(Node.MoveType type, MovementEvaluator evaluator) {
        EVALUATORS.register(type, evaluator);
    }

    private static void ensureComplete() {
        for (Node.MoveType type : Node.MoveType.values()) {
            if (!EVALUATORS.contains(type)) {
                throw new IllegalStateException("Missing movement evaluator for " + type);
            }
        }
    }
}
