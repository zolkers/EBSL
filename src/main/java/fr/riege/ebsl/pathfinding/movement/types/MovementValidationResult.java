package fr.riege.ebsl.pathfinding.movement.types;

public record MovementValidationResult(boolean valid, String reason) {
    private static final MovementValidationResult VALID = new MovementValidationResult(true, "");

    public static MovementValidationResult ok() {
        return VALID;
    }

    public static MovementValidationResult invalid(String reason) {
        return new MovementValidationResult(false, reason);
    }
}
