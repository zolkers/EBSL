package fr.riege.ebsl.pathfinding.movement.types;

import fr.riege.ebsl.pathfinding.Node;

public interface PathMovement {
    Node.MoveType type();

    default boolean reducesSprintNearWaypoint() {
        return false;
    }

    default boolean countsAsStairSequence() {
        return false;
    }

    default boolean countsAsAscendingDifficulty() {
        return false;
    }

    default void handleJump(MovementExecutionContext context) {
        context.releaseJump();
    }

    default void handleWaterMovement(WaterMovementContext context) {
    }

    default MovementValidationResult validate(MovementValidationContext context) {
        return MovementValidationResult.ok();
    }
}
