package fr.riege.ebsl.pathfinding.movement.types.evaluation;

import fr.riege.ebsl.pathfinding.Node;

import java.util.EnumMap;
import java.util.Map;

public final class MovementEvaluatorRegistry {
    private static final Map<Node.MoveType, MovementEvaluator> EVALUATORS = new EnumMap<>(Node.MoveType.class);

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
        return EVALUATORS.getOrDefault(type, EVALUATORS.get(Node.MoveType.WALK));
    }

    private static void register(Node.MoveType type, MovementEvaluator evaluator) {
        EVALUATORS.put(type, evaluator);
    }

    private static void ensureComplete() {
        for (Node.MoveType type : Node.MoveType.values()) {
            if (!EVALUATORS.containsKey(type)) {
                throw new IllegalStateException("Missing movement evaluator for " + type);
            }
        }
    }
}
