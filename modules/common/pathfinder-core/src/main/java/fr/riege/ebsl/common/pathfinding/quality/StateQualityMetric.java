package fr.riege.ebsl.common.pathfinding.quality;

import fr.riege.ebsl.common.pathfinding.pathing.result.PathState;

final class StateQualityMetric implements PathQualityMetric {
    @Override
    public String id() {
        return "state";
    }

    @Override
    public PathQualityContribution evaluate(PathQualityContext context) {
        PathState state = context.result() == null ? PathState.FAILED : context.result().getPathState();
        double score = switch (state) {
            case FOUND -> 1.0;
            case FALLBACK, LENGTH_LIMITED -> 0.58;
            case MAX_ITERATIONS_REACHED -> 0.45;
            case ABORTED, FAILED -> 0.0;
        };
        return new PathQualityContribution(id(), score, 3.0, state.name().toLowerCase(java.util.Locale.ROOT));
    }
}
