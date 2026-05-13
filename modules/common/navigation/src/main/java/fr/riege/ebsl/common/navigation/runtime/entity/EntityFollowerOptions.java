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

public record EntityFollowerOptions(
    double walkSpeed,
    double swimSpeed,
    double sprintSpeed,
    double jumpVelocity,
    double waypointReachDistance,
    double verticalReachDistance,
    double finalReachDistance,
    boolean sprint,
    boolean lookAtWaypoint
) {
    public static EntityFollowerOptions defaults() {
        return builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private double walkSpeed = 0.28;
        private double swimSpeed = 0.18;
        private double sprintSpeed = 0.36;
        private double jumpVelocity = 0.42;
        private double waypointReachDistance = 0.55;
        private double verticalReachDistance = 1.1;
        private double finalReachDistance = 0.75;
        private boolean sprint = true;
        private boolean lookAtWaypoint = true;

        public Builder walkSpeed(double value) {
            this.walkSpeed = value;
            return this;
        }

        public Builder swimSpeed(double value) {
            this.swimSpeed = value;
            return this;
        }

        public Builder sprintSpeed(double value) {
            this.sprintSpeed = value;
            return this;
        }

        public Builder jumpVelocity(double value) {
            this.jumpVelocity = value;
            return this;
        }

        public Builder waypointReachDistance(double value) {
            this.waypointReachDistance = value;
            return this;
        }

        public Builder verticalReachDistance(double value) {
            this.verticalReachDistance = value;
            return this;
        }

        public Builder finalReachDistance(double value) {
            this.finalReachDistance = value;
            return this;
        }

        public Builder sprint(boolean value) {
            this.sprint = value;
            return this;
        }

        public Builder lookAtWaypoint(boolean value) {
            this.lookAtWaypoint = value;
            return this;
        }

        public EntityFollowerOptions build() {
            return new EntityFollowerOptions(
                Math.max(0.0, walkSpeed),
                Math.max(0.0, swimSpeed),
                Math.max(0.0, sprintSpeed),
                Math.max(0.0, jumpVelocity),
                Math.max(0.05, waypointReachDistance),
                Math.max(0.05, verticalReachDistance),
                Math.max(0.05, finalReachDistance),
                sprint,
                lookAtWaypoint);
        }
    }
}
