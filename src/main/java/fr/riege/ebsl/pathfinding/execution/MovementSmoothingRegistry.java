package fr.riege.ebsl.pathfinding.execution;

import fr.riege.ebsl.pathfinding.Node;
import fr.riege.ebsl.registry.EnumRegistry;

import java.util.List;
import java.util.Optional;

final class MovementSmoothingRegistry {
    private static final EnumRegistry<Node.MoveType, MovementSmoothing> STRATEGIES =
        new EnumRegistry<>(Node.MoveType.class, null);

    private MovementSmoothingRegistry() {
    }

    static Optional<MovementSmoothing.Plan> resolve(List<Node> path, int pursuitSegment, boolean alreadyRotating) {
        if (path == null || path.isEmpty()) {
            return Optional.empty();
        }

        int start = Math.max(0, pursuitSegment);
        int end = Math.min(path.size() - 1, start + 2);
        for (int i = start; i <= end; i++) {
            MovementSmoothing strategy = STRATEGIES.get(path.get(i).moveType);
            if (strategy != null && strategy.applies(path, pursuitSegment)) {
                return Optional.of(strategy.plan(path, pursuitSegment, alreadyRotating));
            }
        }
        return Optional.empty();
    }

    private static void register(Node.MoveType moveType, MovementSmoothing strategy) {
        STRATEGIES.register(moveType, strategy);
    }
}
