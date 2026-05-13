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

package fr.riege.ebsl.common.pathfinding.pathing;

import fr.riege.ebsl.common.pathfinding.pathing.context.EnvironmentContext;
import fr.riege.ebsl.common.pathfinding.pathing.result.PathfinderResult;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;
import java.util.concurrent.CompletionStage;

/**
 * Finds paths asynchronously between navigation positions.
 *
 * <p>Implementations own search state, processor coordination, result reporting, and abort behavior for the active planning request.</p>
 */
public interface Pathfinder {
    /**
     * Starts an asynchronous path search for the supplied navigation endpoints.
 *
     * @param start the starting path position
     * @param target the target path position
     * @return a completion stage that resolves to the asynchronous operation result
     */
    default CompletionStage<PathfinderResult> findPath(PathPosition start, PathPosition target) {
        return findPath(start, target, null);
    }

    /**
     * Starts an asynchronous path search for the supplied navigation endpoints.
 *
     * @param start the starting path position
     * @param target the target path position
     * @param context the context describing the operation being performed
     * @return a completion stage that resolves to the asynchronous operation result
     */
    CompletionStage<PathfinderResult> findPath(PathPosition start, PathPosition target,
                                               EnvironmentContext context);

    /**
     * Requests cancellation of the active operation.
     */
    void abort();
}
