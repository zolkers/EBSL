package fr.riege.ebsl.pathfinding.parkour;

public record ParkourJumpPlan(
    boolean feasible,
    int approachBlocks,
    int distanceBlocks,
    double requiredReach,
    double estimatedReach,
    double verticalDelta,
    String reason
) {
    public static ParkourJumpPlan rejected(String reason) {
        return new ParkourJumpPlan(false, 0, 0, 0.0, 0.0, 0.0, reason);
    }
}
