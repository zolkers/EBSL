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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Creates path instances without exposing their implementation class.
 */
public final class Paths {
    private Paths() {
    }

    /**
     * Creates an immutable path from the supplied endpoints and positions.
     *
     * @param start the requested start position
     * @param end the requested end position
     * @param positions the ordered path positions
     * @return a path view over the supplied positions
     */
    public static Path of(PathPosition start, PathPosition end, Collection<PathPosition> positions) {
        return new ImmutablePath(
            Objects.requireNonNull(start, "start"),
            Objects.requireNonNull(end, "end"),
            Objects.requireNonNull(positions, "positions"));
    }

    private record ImmutablePath(PathPosition start, PathPosition end, List<PathPosition> positions) implements Path {
        private ImmutablePath(PathPosition start, PathPosition end, Collection<PathPosition> positions) {
            this(start, end, positions instanceof List<PathPosition> list ? List.copyOf(list) : List.copyOf(positions));
        }

        @Override
        public int length() {
            return positions.size();
        }

        @Override
        public PathPosition getStart() {
            return start;
        }

        @Override
        public PathPosition getEnd() {
            return end;
        }

        @Override
        public Collection<PathPosition> collect() {
            return Collections.unmodifiableList(positions);
        }

        @Override
        public Iterator<PathPosition> iterator() {
            return positions.iterator();
        }
    }
}
