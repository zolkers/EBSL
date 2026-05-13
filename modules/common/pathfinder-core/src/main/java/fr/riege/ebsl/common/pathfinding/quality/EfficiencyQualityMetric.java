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
