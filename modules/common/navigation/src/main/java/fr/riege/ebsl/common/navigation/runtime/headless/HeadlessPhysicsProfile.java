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

package fr.riege.ebsl.common.navigation.runtime.headless;

public record HeadlessPhysicsProfile(
    double slipperiness,
    double horizontalDrag,
    double verticalDrag,
    double gravity,
    double jumpVelocity,
    double maxClimbVelocity,
    double stepHeight
) {
    public static final HeadlessPhysicsProfile DEFAULT = builder().build();
    public static final HeadlessPhysicsProfile ICE = builder().slipperiness(0.98).horizontalDrag(0.98).build();
    public static final HeadlessPhysicsProfile SOUL_SAND = builder().horizontalDrag(0.4).build();
    public static final HeadlessPhysicsProfile HONEY = builder().horizontalDrag(0.4).stepHeight(0.5).build();
    public static final HeadlessPhysicsProfile WATER = builder().horizontalDrag(0.8).verticalDrag(0.8).gravity(0.02).build();
    public static final HeadlessPhysicsProfile LAVA = builder().horizontalDrag(0.5).verticalDrag(0.5).gravity(0.02).build();
    public static final HeadlessPhysicsProfile CLIMBABLE = builder().maxClimbVelocity(0.2).build();

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private double slipperiness = 0.6;
        private double horizontalDrag = 0.91;
        private double verticalDrag = 0.98;
        private double gravity = 0.08;
        private double jumpVelocity = 0.42;
        private double maxClimbVelocity = 0.15;
        private double stepHeight = 1.0;

        public Builder slipperiness(double value) {
            slipperiness = Math.max(0.0, value);
            return this;
        }

        public Builder horizontalDrag(double value) {
            horizontalDrag = Math.max(0.0, value);
            return this;
        }

        public Builder verticalDrag(double value) {
            verticalDrag = Math.max(0.0, value);
            return this;
        }

        public Builder gravity(double value) {
            gravity = Math.max(0.0, value);
            return this;
        }

        public Builder jumpVelocity(double value) {
            jumpVelocity = Math.max(0.0, value);
            return this;
        }

        public Builder maxClimbVelocity(double value) {
            maxClimbVelocity = Math.max(0.0, value);
            return this;
        }

        public Builder stepHeight(double value) {
            stepHeight = Math.clamp(value, 0.0, 1.5);
            return this;
        }

        public HeadlessPhysicsProfile build() {
            return new HeadlessPhysicsProfile(
                slipperiness,
                horizontalDrag,
                verticalDrag,
                gravity,
                jumpVelocity,
                maxClimbVelocity,
                stepHeight);
        }
    }
}
