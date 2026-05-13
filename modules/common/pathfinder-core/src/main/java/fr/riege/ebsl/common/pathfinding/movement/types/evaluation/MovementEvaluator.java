package fr.riege.ebsl.common.pathfinding.movement.types.evaluation;

/**
 * Defines the contract for {@code MovementEvaluator} implementations.
 */
public interface MovementEvaluator {
    default boolean reducesSprintNearWaypoint() {
        return false;
    }

    default boolean countsAsStairSequence() {
        return false;
    }

    default boolean countsAsAscendingDifficulty() {
        return false;
    }

    default MovementValidationResult validate(MovementValidationContext context) {
        return MovementValidationResult.ok();
    }
}
