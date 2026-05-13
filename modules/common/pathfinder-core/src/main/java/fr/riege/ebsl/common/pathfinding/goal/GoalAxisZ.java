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

package fr.riege.ebsl.common.pathfinding.goal;

public record GoalAxisZ(int z) implements Goal {
    @Override
    public boolean isInGoal(int x, int y, int z) {
        return this.z == z;
    }

    @Override
    public double heuristic(int x, int y, int z) {
        return Math.abs(this.z - z);
    }

    @Override
    public String debugName() {
        return "GoalAxisZ[" + z + "]";
    }

    @Override
    public NavigationTarget resolve(int px, int py, int pz) {
        return new NavigationTarget.Column(px, z);
    }
}
