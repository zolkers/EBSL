package fr.riege.ebsl.common.pathfinding.movement.types.evaluation;

import fr.riege.ebsl.common.registry.EnumRegistry;
import fr.riege.ebsl.common.pathfinding.Node;

public final class MovementEvaluatorRegistry {
    private static final EnumRegistry<Node.MoveType, MovementEvaluator> EVALUATORS =
        new EnumRegistry<>(Node.MoveType.class, new WalkMovementEvaluator());

    static {
        register(Node.MoveType.WALK, new WalkMovementEvaluator());
        register(Node.MoveType.WALK_DIAGONAL, new WalkDiagonalMovementEvaluator());
        register(Node.MoveType.STEP_UP, new StepUpMovementEvaluator());
        register(Node.MoveType.JUMP, new JumpMovementEvaluator());
        register(Node.MoveType.PARKOUR, new ParkourMovementEvaluator());
        register(Node.MoveType.FALL, new FallMovementEvaluator());
        register(Node.MoveType.SWIM, new SwimMovementEvaluator());
        register(Node.MoveType.CLIMB, new ClimbMovementEvaluator());
        register(Node.MoveType.FLY, new FlyMovementEvaluator());
        ensureComplete();
    }

    private MovementEvaluatorRegistry() {
    }

    public static MovementEvaluator get(Node.MoveType type) {
        return EVALUATORS.get(type);
    }

    private static void register(Node.MoveType type, MovementEvaluator evaluator) {
        EVALUATORS.register(type, evaluator);
    }

    private static void ensureComplete() {
        for (Node.MoveType type : Node.MoveType.values()) {
            if (!EVALUATORS.contains(type)) {
                throw new IllegalStateException("Missing movement evaluator for " + type);
            }
        }
    }
}
