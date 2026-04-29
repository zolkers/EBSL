package fr.riege.ebsl.pathfinding.execution;

import fr.riege.ebsl.pathfinding.Node;
import fr.riege.ebsl.registry.EnumRegistry;

final class MovementRecoveryRegistry {
    private static final MovementRecoveryProfile DEFAULT = new WalkRecoveryProfile();
    private static final EnumRegistry<Node.MoveType, MovementRecoveryProfile> PROFILES =
        new EnumRegistry<>(Node.MoveType.class, DEFAULT);

    static {
        register(Node.MoveType.WALK, DEFAULT);
        register(Node.MoveType.WALK_DIAGONAL, DEFAULT);
        register(Node.MoveType.STEP_UP, DEFAULT);
        register(Node.MoveType.JUMP, DEFAULT);
        register(Node.MoveType.PARKOUR, new ParkourRecoveryProfile());
        register(Node.MoveType.FALL, DEFAULT);
        register(Node.MoveType.SWIM, DEFAULT);
        register(Node.MoveType.CLIMB, DEFAULT);
        register(Node.MoveType.FLY, DEFAULT);
    }

    private MovementRecoveryRegistry() {
    }

    static MovementRecoveryProfile get(Node.MoveType moveType) {
        return PROFILES.get(moveType);
    }

    private static void register(Node.MoveType moveType, MovementRecoveryProfile profile) {
        PROFILES.register(moveType, profile);
    }
}
