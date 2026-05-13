package fr.riege.ebsl.common.pathfinding.movement.types.execution;

/**
 * Defines the contract for {@code MovementExecutor} implementations.
 */
public interface MovementExecutor {
    default void handleJump(MovementExecutionContext context) {
        context.releaseJump();
    }

    default void handleWaterMovement(WaterMovementContext context) {
    }
}
