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

/**
 * Defines a pathfinding objective in block-space coordinates.
 *
 * <p>Goals provide membership tests, heuristics, debugging names, and concrete targets for planners and UI layers.</p>
 */
public interface Goal {
    /**
     * Returns whether in goal is true for the current state.
 *
     * @param x the block x coordinate
     * @param y the block y coordinate
     * @param z the block z coordinate
     * @return true when the condition is satisfied; false otherwise
     */
    boolean isInGoal(int x, int y, int z);

    /**
     * Returns the estimated remaining cost from the supplied coordinates to this goal.
 *
     * @param x the block x coordinate
     * @param y the block y coordinate
     * @param z the block z coordinate
     * @return the value defined by this contract
     */
    double heuristic(int x, int y, int z);

    /**
     * Returns a concise name used in diagnostics and logs.
 *
     * @return the value defined by this contract
     */
    String debugName();

    /**
     * Resolves this goal into a concrete navigation target from the supplied player position.
 *
     * @param px the current player block x coordinate
     * @param py the current player block y coordinate
     * @param pz the current player block z coordinate
     * @return the value defined by this contract
     */
    NavigationTarget resolve(int px, int py, int pz);
}
