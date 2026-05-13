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
package fr.riege.ebsl.common.navigation.runtime.entity;

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.pathfinding.Node;

public record MovementIntent(
    Vec3d velocity,
    Vec3d lookTarget,
    boolean jump,
    boolean sprint,
    boolean sneak,
    Node.MoveType moveType
) {
    public static MovementIntent stop() {
        return new MovementIntent(
            new Vec3d(0.0, 0.0, 0.0),
            null,
            false,
            false,
            false,
            Node.MoveType.WALK);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Vec3d velocity = new Vec3d(0.0, 0.0, 0.0);
        private Vec3d lookTarget;
        private boolean jump;
        private boolean sprint;
        private boolean sneak;
        private Node.MoveType moveType = Node.MoveType.WALK;

        public Builder velocity(Vec3d value) {
            this.velocity = value == null ? new Vec3d(0.0, 0.0, 0.0) : value;
            return this;
        }

        public Builder lookTarget(Vec3d value) {
            this.lookTarget = value;
            return this;
        }

        public Builder jump(boolean value) {
            this.jump = value;
            return this;
        }

        public Builder sprint(boolean value) {
            this.sprint = value;
            return this;
        }

        public Builder sneak(boolean value) {
            this.sneak = value;
            return this;
        }

        public Builder moveType(Node.MoveType value) {
            this.moveType = value == null ? Node.MoveType.WALK : value;
            return this;
        }

        public MovementIntent build() {
            return new MovementIntent(velocity, lookTarget, jump, sprint, sneak, moveType);
        }
    }
}
