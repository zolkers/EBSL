package fr.riege.ebsl.pathfinding.goal;

final class GoalValidators {
    private GoalValidators() {
    }

    static void requireNonNegativeFiniteRadius(double radius, String name) {
        if (!Double.isFinite(radius) || radius < 0.0) {
            throw new IllegalArgumentException(name + " must be finite and non-negative");
        }
    }
}
