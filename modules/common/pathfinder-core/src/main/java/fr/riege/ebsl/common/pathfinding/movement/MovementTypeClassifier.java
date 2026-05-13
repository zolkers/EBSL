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

package fr.riege.ebsl.common.pathfinding.movement;

import fr.riege.ebsl.common.pathfinding.Node;

/**
 * Classifies a transition between path nodes into the movement type that should own it.
 *
 * <p>The classifier is the shared boundary between planning, quality scoring, and execution so all stages reason about the same movement label.</p>
 */
@FunctionalInterface
public interface MovementTypeClassifier {
    /**
     * Classifies the supplied movement context into the movement type used by planning, quality, and execution.
 *
     * @param context the context describing the operation being performed
     * @return the value defined by this contract
     */
    Node.MoveType classify(MovementClassificationContext context);
}
