package fr.riege.ebsl.common.pathfinding.movement.types.execution;

public interface MovementExecutor {
    default void handleJump(MovementExecutionContext context) {
        context.releaseJump();
    }

    default void handleWaterMovement(WaterMovementContext context) {
    }
}
