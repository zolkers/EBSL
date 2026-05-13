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

import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;

final class ProgressQualityMetric implements PathQualityMetric {
    @Override
    public String id() {
        return "progress";
    }

    @Override
    public PathQualityContribution evaluate(PathQualityContext context) {
        PathPosition start = context.start();
        PathPosition end = context.end();
        if (start == null || end == null || context.result() == null || context.result().getPath() == null) {
            return new PathQualityContribution(id(), 0.0, 2.0, "no path");
        }
        PathPosition target = context.result().getPath().getEnd();
        double total = Math.max(0.0001, start.distance(target));
        double remaining = end.distance(target);
        double score = Math.clamp((total - remaining) / total, 0.0, 1.0);
        return new PathQualityContribution(id(), score, 2.0, String.format("%.0f%%", score * 100.0));
    }
}
