package fr.riege.ebsl.common.pathfinding.quality;

import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;

import java.util.List;

final class SmoothnessQualityMetric implements PathQualityMetric {
    @Override
    public String id() {
        return "smoothness";
    }

    @Override
    public PathQualityContribution evaluate(PathQualityContext context) {
        List<PathPosition> positions = context.positions();
        if (positions.size() < 3) {
            return new PathQualityContribution(id(), positions.isEmpty() ? 0.0 : 1.0, 0.8, "straight");
        }
        int turns = 0;
        int comparable = 0;
        int previousDx = 0;
        int previousDz = 0;
        for (int i = 1; i < positions.size(); i++) {
            int dx = Integer.compare(positions.get(i).flooredX(), positions.get(i - 1).flooredX());
            int dz = Integer.compare(positions.get(i).flooredZ(), positions.get(i - 1).flooredZ());
            if (dx == 0 && dz == 0) {
                continue;
            }
            if (previousDx != 0 || previousDz != 0) {
                comparable++;
                if (dx != previousDx || dz != previousDz) {
                    turns++;
                }
            }
            previousDx = dx;
            previousDz = dz;
        }
        double turnRatio = comparable == 0 ? 0.0 : (double) turns / comparable;
        double score = Math.clamp(1.0 - turnRatio, 0.0, 1.0);
        return new PathQualityContribution(id(), score, 0.8, turns + " turns");
    }
}
