package fr.riege.ebsl.common.pathfinding.movement.types.evaluation;

public record MovementValidationResult(boolean valid, String reason) {
    private static final MovementValidationResult OK_RESULT = new MovementValidationResult(true, "");

    public static MovementValidationResult ok() {
        return OK_RESULT;
    }

    public static MovementValidationResult invalid(String reason) {
        return new MovementValidationResult(false, reason);
    }
}
