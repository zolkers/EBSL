package fr.riege.ebsl.common.pathfinding.quality;

/**
 * Scores one dimension of path quality.
 *
 * <p>Metrics produce named contributions that are aggregated into a quality report for planning and diagnostics.</p>
 */
public interface PathQualityMetric {
    /**
     * Returns the stable identifier used for lookup, persistence, and diagnostics.
 *
     * @return the value defined by this contract
     */
    String id();
    /**
     * Evaluates this contract against the supplied context.
 *
     * @param context the context describing the operation being performed
     * @return the value defined by this contract
     */
    PathQualityContribution evaluate(PathQualityContext context);
}
