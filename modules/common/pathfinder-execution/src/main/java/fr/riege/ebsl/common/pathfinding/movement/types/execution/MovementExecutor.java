package fr.riege.ebsl.common.pathfinding.movement.types.execution;

/**
 * Executes movement-type-specific input behavior.
 *
 * <p>Executors translate classified movement contexts into jump, water, and other control actions during path following.</p>
 */
public interface MovementExecutor {
    /**
     * Handles jump input for the supplied movement execution context.
 *
     * @param context the context describing the operation being performed
     */
    default void handleJump(MovementExecutionContext context) {
        context.releaseJump();
    }

    /**
     * Handles water movement input for the supplied water movement context.
 *
     * @param context the context describing the operation being performed
     */
    default void handleWaterMovement(WaterMovementContext context) {
    }
}
