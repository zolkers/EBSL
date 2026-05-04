package fr.riege.ebsl.pathfinding.movement.types.execution;

public interface MovementExecutor {
    default void handleJump(MovementExecutionContext context) {
        context.releaseJump();
    }

    default void handleWaterMovement(WaterMovementContext context) {
    }
}
