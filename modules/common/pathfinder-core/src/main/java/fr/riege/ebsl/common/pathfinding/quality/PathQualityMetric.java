package fr.riege.ebsl.common.pathfinding.quality;

public interface PathQualityMetric {
    String id();
    PathQualityContribution evaluate(PathQualityContext context);
}
