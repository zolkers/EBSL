package fr.riege.ebsl.common.pathfinding.pathing.result;

import fr.riege.ebsl.common.pathfinding.quality.PathQualityReport;
import fr.riege.ebsl.common.pathfinding.result.PathfinderResultImpl;

import java.util.Objects;

/**
 * Creates pathfinder result instances without exposing their implementation class.
 */
public final class PathfinderResults {
    private PathfinderResults() {
    }

    /**
     * Creates a pathfinder result.
     *
     * @param state the result state
     * @param path the associated path
     * @return a pathfinder result
     */
    public static PathfinderResult of(PathState state, Path path) {
        return new PathfinderResultImpl(Objects.requireNonNull(state, "state"), path);
    }

    /**
     * Creates a pathfinder result with an attached quality report.
     *
     * @param state the result state
     * @param path the associated path
     * @param quality the quality report to attach
     * @return a pathfinder result
     */
    public static PathfinderResult of(PathState state, Path path, PathQualityReport quality) {
        return new PathfinderResultImpl(Objects.requireNonNull(state, "state"), path, quality);
    }
}
