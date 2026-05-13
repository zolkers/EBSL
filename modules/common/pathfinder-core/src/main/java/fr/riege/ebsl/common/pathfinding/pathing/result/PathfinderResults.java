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

package fr.riege.ebsl.common.pathfinding.pathing.result;

import fr.riege.ebsl.common.pathfinding.quality.PathQualityReport;

import java.util.Objects;

/**
 * Creates pathfinder result instances without exposing their implementation class.
 */
public final class PathfinderResults {
    private PathfinderResults() {
    }

    /**
     * Creates a pathfinder result.
     *
     * @param state the result state
     * @param path the associated path
     * @return a pathfinder result
     */
    public static PathfinderResult of(PathState state, Path path) {
        return of(state, path, PathQualityReport.UNKNOWN);
    }

    /**
     * Creates a pathfinder result with an attached quality report.
     *
     * @param state the result state
     * @param path the associated path
     * @param quality the quality report to attach
     * @return a pathfinder result
     */
    public static PathfinderResult of(PathState state, Path path, PathQualityReport quality) {
        return new ImmutablePathfinderResult(
            Objects.requireNonNull(state, "state"),
            path,
            quality == null ? PathQualityReport.UNKNOWN : quality);
    }

    private record ImmutablePathfinderResult(PathState state, Path path, PathQualityReport quality)
        implements PathfinderResult {
        @Override
        public boolean successful() {
            return state == PathState.FOUND;
        }

        @Override
        public boolean hasFailed() {
            return state == PathState.FAILED;
        }

        @Override
        public boolean hasFallenBack() {
            return state == PathState.FALLBACK
                || state == PathState.MAX_ITERATIONS_REACHED
                || state == PathState.LENGTH_LIMITED;
        }

        @Override
        public PathState getPathState() {
            return state;
        }

        @Override
        public Path getPath() {
            return path;
        }
    }
}
