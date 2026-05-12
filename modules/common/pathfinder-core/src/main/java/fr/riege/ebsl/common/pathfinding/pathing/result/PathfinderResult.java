package fr.riege.ebsl.common.pathfinding.pathing.result;

import fr.riege.ebsl.common.pathfinding.quality.PathQualityReport;

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
