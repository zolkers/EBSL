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
