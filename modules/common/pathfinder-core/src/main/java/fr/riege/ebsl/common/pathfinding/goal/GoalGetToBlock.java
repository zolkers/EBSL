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

public record GoalGetToBlock(int x, int y, int z) implements Goal {
    @Override
    public boolean isInGoal(int x, int y, int z) {
        int dx = Math.abs(this.x - x);
        int dy = Math.abs((this.y + 1) - y);
        int dz = Math.abs(this.z - z);
        return dx <= 1 && dz <= 1 && dy <= 1;
    }

    @Override
    public double heuristic(int x, int y, int z) {
        double dx = Math.max(0, Math.abs(this.x - x) - 1);
        double dy = Math.abs((this.y + 1) - y);
        double dz = Math.max(0, Math.abs(this.z - z) - 1);
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    @Override
    public String debugName() {
        return "GoalGetToBlock[" + x + "," + y + "," + z + "]";
    }

    @Override
    public NavigationTarget resolve(int px, int py, int pz) {
        return new NavigationTarget.Block(x, y, z);
    }
}
