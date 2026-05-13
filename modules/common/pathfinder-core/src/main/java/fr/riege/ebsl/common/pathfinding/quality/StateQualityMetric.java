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
package fr.riege.ebsl.common.pathfinding.quality;

import fr.riege.ebsl.common.pathfinding.pathing.result.PathState;

import java.util.Locale;

final class StateQualityMetric implements PathQualityMetric {
    @Override
    public String id() {
        return "state";
    }

    @Override
    public PathQualityContribution evaluate(PathQualityContext context) {
        PathState state = context.result() == null ? PathState.FAILED : context.result().getPathState();
        double score = switch (state) {
            case FOUND -> 1.0;
            case FALLBACK, LENGTH_LIMITED -> 0.58;
            case MAX_ITERATIONS_REACHED -> 0.45;
            case ABORTED, FAILED -> 0.0;
        };
        return new PathQualityContribution(id(), score, 3.0, state.name().toLowerCase(Locale.ROOT));
    }
}
