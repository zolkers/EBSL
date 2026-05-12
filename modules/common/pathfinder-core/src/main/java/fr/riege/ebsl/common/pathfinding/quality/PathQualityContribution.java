package fr.riege.ebsl.common.pathfinding.quality;

public record PathQualityContribution(String metricId, double score, double weight, String detail) {
    public PathQualityContribution {
        metricId = metricId == null ? "" : metricId;
        score = Math.clamp(score, 0.0, 1.0);
        weight = Math.max(0.0, weight);
        detail = detail == null ? "" : detail;
    }

    double weightedScore() {
        return score * weight;
    }
}
