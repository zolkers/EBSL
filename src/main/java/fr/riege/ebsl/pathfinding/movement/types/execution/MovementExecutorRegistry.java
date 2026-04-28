package fr.riege.ebsl.pathfinding.movement.types.execution;

import fr.riege.ebsl.pathfinding.Node;

import java.util.EnumMap;
import java.util.Map;

public final class MovementExecutorRegistry {
    private static final Map<Node.MoveType, MovementExecutor> EXECUTORS = new EnumMap<>(Node.MoveType.class);

    static {
        register(Node.MoveType.WALK, new WalkMovementExecutor());
        register(Node.MoveType.WALK_DIAGONAL, new WalkDiagonalMovementExecutor());
        register(Node.MoveType.STEP_UP, new StepUpMovementExecutor());
        register(Node.MoveType.JUMP, new JumpMovementExecutor());
        register(Node.MoveType.PARKOUR, new ParkourMovementExecutor());
        register(Node.MoveType.FALL, new FallMovementExecutor());
        register(Node.MoveType.SWIM, new SwimMovementExecutor());
        register(Node.MoveType.CLIMB, new ClimbMovementExecutor());
        register(Node.MoveType.FLY, new FlyMovementExecutor());
        ensureComplete();
    }

    private MovementExecutorRegistry() {
    }

    public static MovementExecutor get(Node.MoveType type) {
        return EXECUTORS.getOrDefault(type, EXECUTORS.get(Node.MoveType.WALK));
    }

    private static void register(Node.MoveType type, MovementExecutor executor) {
        EXECUTORS.put(type, executor);
    }

    private static void ensureComplete() {
        for (Node.MoveType type : Node.MoveType.values()) {
            if (!EXECUTORS.containsKey(type)) {
                throw new IllegalStateException("Missing movement executor for " + type);
            }
        }
    }
}
