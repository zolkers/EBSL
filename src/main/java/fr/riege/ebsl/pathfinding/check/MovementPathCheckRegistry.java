package fr.riege.ebsl.pathfinding.check;

import fr.riege.ebsl.pathfinding.Node;
import fr.riege.ebsl.registry.EnumRegistry;

import java.util.List;

final class MovementPathCheckRegistry {
    private static final EnumRegistry<Node.MoveType, List<PathCheck>> CHECKS =
        new EnumRegistry<>(Node.MoveType.class, List.of());

    static {
        register(Node.MoveType.PARKOUR, List.of());
    }

    private MovementPathCheckRegistry() {
    }

    static PathCheckResult evaluate(PathCheckContext context) {
        for (PathCheck check : CHECKS.get(context.currentMoveType())) {
            PathCheckResult result = check.evaluate(context);
            if (result.requiresAction()) {
                return result;
            }
        }
        if (context.currentMoveType() != Node.MoveType.PARKOUR
            && context.hasMoveTypeInWindow(Node.MoveType.PARKOUR, 2)) {
            for (PathCheck check : CHECKS.get(Node.MoveType.PARKOUR)) {
                PathCheckResult result = check.evaluate(context);
                if (result.requiresAction()) {
                    return result;
                }
            }
        }
        return PathCheckResult.none();
    }

    private static void register(Node.MoveType moveType, List<PathCheck> checks) {
        CHECKS.register(moveType, List.copyOf(checks));
    }
}
