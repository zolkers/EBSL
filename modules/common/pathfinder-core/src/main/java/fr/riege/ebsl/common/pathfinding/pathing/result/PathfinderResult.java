package fr.riege.ebsl.common.pathfinding.pathing.result;

import fr.riege.ebsl.common.pathfinding.quality.PathQualityReport;

/**
 * Defines the contract for {@code PathfinderResult} implementations.
 */
public interface PathfinderResult {
    boolean successful();
    boolean hasFailed();
    boolean hasFallenBack();
    PathState getPathState();
    Path getPath();

    default PathQualityReport quality() {
        return PathQualityReport.UNKNOWN;
    }
}
