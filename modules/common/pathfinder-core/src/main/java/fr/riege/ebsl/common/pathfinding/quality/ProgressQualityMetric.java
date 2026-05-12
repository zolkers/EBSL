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
