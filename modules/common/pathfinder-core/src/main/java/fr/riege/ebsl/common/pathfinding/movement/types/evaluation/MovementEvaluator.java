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

package fr.riege.ebsl.common.pathfinding.movement.types.evaluation;

/**
 * Validates and describes the planning behavior of one movement type.
 *
 * <p>Evaluators decide whether a candidate transition is legal and expose policy hints used by smoothing, costs, and execution.</p>
 */
public interface MovementEvaluator {
    /**
     * Returns whether sprint should be reduced near waypoints for this movement type.
 *
     * @return true when the condition is satisfied; false otherwise
     */
    default boolean reducesSprintNearWaypoint() {
        return false;
    }

    /**
     * Returns whether this movement type contributes to stair-sequence detection.
 *
     * @return true when the condition is satisfied; false otherwise
     */
    default boolean countsAsStairSequence() {
        return false;
    }

    /**
     * Returns whether this movement type contributes to ascending-difficulty scoring.
 *
     * @return true when the condition is satisfied; false otherwise
     */
    default boolean countsAsAscendingDifficulty() {
        return false;
    }

    /**
     * Validates a candidate movement transition and returns the validation result.
 *
     * @param context the context describing the operation being performed
     * @return the value defined by this contract
     */
    default MovementValidationResult validate(MovementValidationContext context) {
        return MovementValidationResult.ok();
    }
}
