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

import fr.riege.ebsl.common.core.registry.MapRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class PathQualityRegistry {
    private static final MapRegistry<String, PathQualityMetric> METRICS = new MapRegistry<>(null);

    static {
        register(new StateQualityMetric());
        register(new ProgressQualityMetric());
        register(new EfficiencyQualityMetric());
        register(new MovementRiskQualityMetric());
        register(new SmoothnessQualityMetric());
        register(new TerrainOpportunityQualityMetric());
    }

    private PathQualityRegistry() {
    }

    public static PathQualityReport evaluate(PathQualityContext context) {
        if (context == null) {
            return PathQualityReport.UNKNOWN;
        }
        List<PathQualityContribution> contributions = new ArrayList<>();
        for (PathQualityMetric metric : METRICS.values()) {
            contributions.add(metric.evaluate(context));
        }
        return PathQualityReport.of(contributions);
    }

    public static void register(PathQualityMetric metric) {
        METRICS.register(normalize(metric.id()), metric);
    }

    private static String normalize(String id) {
        return id == null ? "" : id.trim().toLowerCase(Locale.ROOT).replace('-', '_');
    }
}
