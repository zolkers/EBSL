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
package fr.riege.ebsl.common.pathfinding.provider;

/**
 * Describes the navigation-relevant properties of one world cell.
 *
 * <p>Pathfinding code uses these flags instead of querying raw blocks directly during movement validation.</p>
 */
public interface NavigationPoint {
    /**
     * Returns whether traversable is true for the current state.
 *
     * @return true when the condition is satisfied; false otherwise
     */
    boolean isTraversable();
    /**
     * Returns whether this navigation point has usable floor support.
 *
     * @return true when the condition is satisfied; false otherwise
     */
    boolean hasFloor();
    /**
     * Returns the floor height used for movement and step calculations.
 *
     * @return the value defined by this contract
     */
    double getFloorLevel();
    /**
     * Returns whether the block at the supplied coordinates can be climbed.
 *
     * @return true when the condition is satisfied; false otherwise
     */
    boolean isClimbable();
    /**
     * Returns whether liquid is true for the current state.
 *
     * @return true when the condition is satisfied; false otherwise
     */
    boolean isLiquid();
}
