package fr.riege.ebsl.pathfinding.check;

import java.util.List;

public final class PathCheckRegistry {
    private static final List<PathCheck> CHECKS = List.of(
        new AnomalousPathCutoffCheck(),
        new HugeDeviationCheck(),
        new SustainedOffPathCheck(),
        new SmartCutoffCheck()
    );

    private PathCheckRegistry() {
    }

    public static PathCheckResult evaluate(PathCheckContext context) {
        for (PathCheck check : CHECKS) {
            PathCheckResult result = check.evaluate(context);
            if (result.requiresAction()) {
                return result;
            }
        }
        return PathCheckResult.none();
    }
}
