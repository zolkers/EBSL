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
package fr.riege.ebsl.common.pathfinding.pathing.result;

import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;
import java.util.Collection;

/**
 * Represents an ordered sequence of positions returned by the pathfinder.
 *
 * <p>Paths are iterable and expose start/end metadata while preserving collection access for rendering and execution code.</p>
 */
public interface Path extends Iterable<PathPosition> {
    /**
     * Returns the number of positions contained in the path.
 *
     * @return the value defined by this contract
     */
    int length();
    /**
     * Returns the first position in the path.
 *
     * @return the value defined by this contract
     */
    PathPosition getStart();
    /**
     * Returns the final position in the path.
 *
     * @return the value defined by this contract
     */
    PathPosition getEnd();
    /**
     * Returns the path positions as a collection for consumers that cannot iterate lazily.
 *
     * @return the requested values
     */
    Collection<PathPosition> collect();
}
