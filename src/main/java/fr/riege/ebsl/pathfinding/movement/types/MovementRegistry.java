package fr.riege.ebsl.pathfinding.movement.types;

import fr.riege.ebsl.pathfinding.Node;

import java.util.EnumMap;
import java.util.Map;

public final class MovementRegistry {
    private static final Map<Node.MoveType, PathMovement> MOVEMENTS = new EnumMap<>(Node.MoveType.class);

    static {
        register(new WalkMovement());
        register(new WalkDiagonalMovement());
        register(new StepUpMovement());
        register(new JumpMovement());
        register(new ParkourMovement());
        register(new FallMovement());
        register(new SwimMovement());
        register(new ClimbMovement());
        register(new FlyMovement());
    }

    private MovementRegistry() {
    }

    public static PathMovement get(Node.MoveType type) {
        return MOVEMENTS.getOrDefault(type, MOVEMENTS.get(Node.MoveType.WALK));
    }

    private static void register(PathMovement movement) {
        MOVEMENTS.put(movement.type(), movement);
    }
}
