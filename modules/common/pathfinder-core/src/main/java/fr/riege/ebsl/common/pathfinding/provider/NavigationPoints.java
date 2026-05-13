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
 * Creates navigation point values without exposing their storage implementation.
 */
public final class NavigationPoints {
    private static final NavigationPoint BLOCKED = new NavigationPointImpl(false, false, 0.0, false, false);
    private static final NavigationPoint OPEN_FLOOR = new NavigationPointImpl(true, true, 0.0, false, false);

    private NavigationPoints() {
    }

    /**
     * Returns a shared blocked navigation point.
     *
     * @return a blocked navigation point
     */
    public static NavigationPoint blocked() {
        return BLOCKED;
    }

    /**
     * Returns a shared open-floor navigation point.
     *
     * @return an open-floor navigation point
     */
    public static NavigationPoint openFloor() {
        return OPEN_FLOOR;
    }

    /**
     * Creates a navigation point.
     *
     * @param traversable whether the actor may occupy the point
     * @param floor whether the point has usable support
     * @param floorLevel the resolved top surface level
     * @param climbable whether the point can be climbed
     * @param liquid whether the point is liquid
     * @return a navigation point
     */
    public static NavigationPoint of(boolean traversable, boolean floor, double floorLevel,
                                     boolean climbable, boolean liquid) {
        return new NavigationPointImpl(traversable, floor, floorLevel, climbable, liquid);
    }
}
