package fr.riege.ebsl.common.pathfinding.pathing.result;

import fr.riege.ebsl.common.pathfinding.quality.PathQualityReport;

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
        return of(state, path, PathQualityReport.UNKNOWN);
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
        return new ImmutablePathfinderResult(
            Objects.requireNonNull(state, "state"),
            path,
            quality == null ? PathQualityReport.UNKNOWN : quality);
    }

    private record ImmutablePathfinderResult(PathState state, Path path, PathQualityReport quality)
        implements PathfinderResult {
        @Override
        public boolean successful() {
            return state == PathState.FOUND;
        }

        @Override
        public boolean hasFailed() {
            return state == PathState.FAILED;
        }

        @Override
        public boolean hasFallenBack() {
            return state == PathState.FALLBACK
                || state == PathState.MAX_ITERATIONS_REACHED
                || state == PathState.LENGTH_LIMITED;
        }

        @Override
        public PathState getPathState() {
            return state;
        }

        @Override
        public Path getPath() {
            return path;
        }
    }
}
