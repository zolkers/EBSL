package fr.riege.ebsl.common.pathfinding.quality;

/**
 * Defines the contract for {@code PathQualityMetric} implementations.
 */
public interface PathQualityMetric {
    String id();
    PathQualityContribution evaluate(PathQualityContext context);
}
