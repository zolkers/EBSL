package fr.riege.ebsl.common.pathfinding.quality;

import java.util.List;

public record PathQualityReport(double score, PathQualityGrade grade, List<PathQualityContribution> contributions) {
    public static final PathQualityReport UNKNOWN = new PathQualityReport(0.0, PathQualityGrade.FAILED, List.of());

    public PathQualityReport {
        score = Math.clamp(score, 0.0, 1.0);
        grade = grade == null ? grade(score) : grade;
        contributions = contributions == null ? List.of() : List.copyOf(contributions);
    }

    public static PathQualityReport of(List<PathQualityContribution> contributions) {
        if (contributions == null || contributions.isEmpty()) {
            return UNKNOWN;
        }
        double weighted = 0.0;
        double totalWeight = 0.0;
        for (PathQualityContribution contribution : contributions) {
            weighted += contribution.weightedScore();
            totalWeight += contribution.weight();
        }
        double score = totalWeight <= 0.0 ? 0.0 : weighted / totalWeight;
        return new PathQualityReport(score, grade(score), contributions);
    }

    private static PathQualityGrade grade(double score) {
        if (score >= 0.85) return PathQualityGrade.EXCELLENT;
        if (score >= 0.70) return PathQualityGrade.GOOD;
        if (score >= 0.45) return PathQualityGrade.RISKY;
        if (score > 0.0) return PathQualityGrade.POOR;
        return PathQualityGrade.FAILED;
    }
}
