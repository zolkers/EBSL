package fr.riege.ebsl.common.pathfinding.check;

import fr.riege.ebsl.common.core.registry.MapRegistry;
import fr.riege.ebsl.common.pathfinding.annotation.PathingStage;

@PathingStage(PathingStage.Stage.RECOVERY)
public final class PathCheckRegistry {
    private static final MapRegistry<String, PathCheck> CHECKS = new MapRegistry<>(null);

    static {
        register("anomalous_cutoff", new AnomalousPathCutoffCheck());
        register("huge_deviation", new HugeDeviationCheck());
        register("sustained_off_path", new SustainedOffPathCheck());
        register("smart_cutoff", new SmartCutoffCheck());
    }

    private PathCheckRegistry() {
    }

    public static PathCheckResult evaluate(PathCheckContext context) {
        PathCheckResult movementResult = MovementPathCheckRegistry.evaluate(context);
        if (movementResult.requiresAction()) {
            return movementResult;
        }
        for (PathCheck check : CHECKS.values()) {
            PathCheckResult result = check.evaluate(context);
            if (result.requiresAction()) {
                return result;
            }
        }
        return PathCheckResult.none();
    }

    private static void register(String id, PathCheck check) {
        CHECKS.register(id, check);
    }
}
