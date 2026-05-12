package fr.riege.ebsl.common.pathfinding.result;

import fr.riege.ebsl.common.pathfinding.pathing.result.Path;
import fr.riege.ebsl.common.pathfinding.pathing.result.PathState;
import fr.riege.ebsl.common.pathfinding.pathing.result.PathfinderResult;
import fr.riege.ebsl.common.pathfinding.quality.PathQualityReport;

public final class PathfinderResultImpl implements PathfinderResult {
    private final PathState state;
    private final Path path;
    private final PathQualityReport quality;

    public PathfinderResultImpl(PathState state, Path path) {
        this(state, path, PathQualityReport.UNKNOWN);
    }

    public PathfinderResultImpl(PathState state, Path path, PathQualityReport quality) {
        this.state = state;
        this.path  = path;
        this.quality = quality == null ? PathQualityReport.UNKNOWN : quality;
    }

    @Override public boolean successful()    { return state == PathState.FOUND; }
    @Override public boolean hasFailed()     { return state == PathState.FAILED; }
    @Override public boolean hasFallenBack() {
        return state == PathState.FALLBACK
            || state == PathState.MAX_ITERATIONS_REACHED
            || state == PathState.LENGTH_LIMITED;
    }
    @Override public PathState getPathState() { return state; }
    @Override public Path getPath()           { return path; }
    @Override public PathQualityReport quality() { return quality; }

    public PathfinderResultImpl withQuality(PathQualityReport quality) {
        return new PathfinderResultImpl(state, path, quality);
    }
}
