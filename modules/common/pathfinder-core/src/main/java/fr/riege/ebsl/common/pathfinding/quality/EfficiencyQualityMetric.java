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

package fr.riege.ebsl.common.pathfinding.quality;

import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;

final class EfficiencyQualityMetric implements PathQualityMetric {
    @Override
    public String id() {
        return "efficiency";
    }

    @Override
    public PathQualityContribution evaluate(PathQualityContext context) {
        PathPosition start = context.start();
        PathPosition end = context.end();
        if (start == null || end == null || context.pathLength() <= 0.0) {
            return new PathQualityContribution(id(), 0.0, 1.2, "no distance");
        }
        double direct = Math.max(0.0001, start.distance(end));
        double ratio = context.pathLength() / direct;
        double score = Math.clamp(1.0 / ratio, 0.0, 1.0);
        return new PathQualityContribution(id(), score, 0.8, String.format("x%.2f", ratio));
    }
}
