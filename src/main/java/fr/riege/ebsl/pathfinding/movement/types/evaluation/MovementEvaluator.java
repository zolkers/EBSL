package fr.riege.ebsl.pathfinding.movement.types.evaluation;

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
