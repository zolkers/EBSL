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
package fr.riege.ebsl.common.pathfinding.pathing.processing;

public final class Cost {
    public final double value;

    public static final Cost ZERO = new Cost(0.0);

    private Cost(double value) {
        this.value = value;
    }

    public static Cost of(double value) {
        if (Double.isNaN(value) || value < 0) {
            throw new IllegalArgumentException("Cost must be a positive number or 0");
        }
        return new Cost(value);
    }
}
