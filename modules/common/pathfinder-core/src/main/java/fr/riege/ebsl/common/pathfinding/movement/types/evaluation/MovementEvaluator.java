package fr.riege.ebsl.common.pathfinding.movement.types.evaluation;

/**
 * Validates and describes the planning behavior of one movement type.
 *
 * <p>Evaluators decide whether a candidate transition is legal and expose policy hints used by smoothing, costs, and execution.</p>
 */
public interface MovementEvaluator {
    /**
     * Returns whether sprint should be reduced near waypoints for this movement type.
 *
     * @return true when the condition is satisfied; false otherwise
     */
    default boolean reducesSprintNearWaypoint() {
        return false;
    }

    /**
     * Returns whether this movement type contributes to stair-sequence detection.
 *
     * @return true when the condition is satisfied; false otherwise
     */
    default boolean countsAsStairSequence() {
        return false;
    }

    /**
     * Returns whether this movement type contributes to ascending-difficulty scoring.
 *
     * @return true when the condition is satisfied; false otherwise
     */
    default boolean countsAsAscendingDifficulty() {
        return false;
    }

    /**
     * Validates a candidate movement transition and returns the validation result.
 *
     * @param context the context describing the operation being performed
     * @return the value defined by this contract
     */
    default MovementValidationResult validate(MovementValidationContext context) {
        return MovementValidationResult.ok();
    }
}
