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

final class MovementRecoveryRegistry {
    private static final MovementRecoveryProfile DEFAULT = new WalkRecoveryProfile();
    private static final EnumRegistry<Node.MoveType, MovementRecoveryProfile> PROFILES =
        new EnumRegistry<>(Node.MoveType.class, DEFAULT);

    static {
        register(Node.MoveType.WALK, DEFAULT);
        register(Node.MoveType.WALK_DIAGONAL, DEFAULT);
        register(Node.MoveType.STEP_UP, DEFAULT);
        register(Node.MoveType.JUMP, DEFAULT);
        register(Node.MoveType.PARKOUR, new ParkourRecoveryProfile());
        register(Node.MoveType.FALL, DEFAULT);
        register(Node.MoveType.SWIM, DEFAULT);
        register(Node.MoveType.CLIMB, DEFAULT);
        register(Node.MoveType.FLY, DEFAULT);
    }

    private MovementRecoveryRegistry() {
    }

    static MovementRecoveryProfile get(Node.MoveType moveType) {
        return PROFILES.get(moveType);
    }

    private static void register(Node.MoveType moveType, MovementRecoveryProfile profile) {
        PROFILES.register(moveType, profile);
    }
}
