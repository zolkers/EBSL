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
package fr.riege.ebsl.common.pathfinding.pathing.heuristic;

public final class HeuristicWeights {
    public final double manhattanWeight;
    public final double octileWeight;
    public final double perpendicularWeight;
    public final double heightWeight;

    public static final HeuristicWeights DEFAULT_WEIGHTS =
        new HeuristicWeights(0.0, 1.0, 0.0, 0.0);

    public HeuristicWeights(double manhattanWeight, double octileWeight,
                             double perpendicularWeight, double heightWeight) {
        this.manhattanWeight = manhattanWeight;
        this.octileWeight = octileWeight;
        this.perpendicularWeight = perpendicularWeight;
        this.heightWeight = heightWeight;
    }
}
