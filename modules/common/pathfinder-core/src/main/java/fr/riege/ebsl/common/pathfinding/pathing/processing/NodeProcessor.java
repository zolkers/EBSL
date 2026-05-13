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

import fr.riege.ebsl.common.pathfinding.pathing.processing.context.EvaluationContext;

/**
 * Participates in validation and cost calculation for candidate path nodes.
 *
 * <p>Processors may reject impossible transitions or add structured cost contributions without owning the full search algorithm.</p>
 */
public interface NodeProcessor extends Processor {
    /**
     * Returns whether valid is true for the current state.
 *
     * @param context the context describing the operation being performed
     * @return true when the condition is satisfied; false otherwise
     */
    default boolean isValid(EvaluationContext context) {
        return true;
    }

    /**
     * Calculates this processor cost contribution for the current candidate transition.
 *
     * @param context the context describing the operation being performed
     * @return the value defined by this contract
     */
    default Cost calculateCostContribution(EvaluationContext context) {
        return Cost.ZERO;
    }
}
