package fr.riege.ebsl.pathfinding.parkour;

public record ParkourJumpPlan(
    boolean feasible,
    int approachBlocks,
    int distanceBlocks,
    double requiredReach,
    double estimatedReach,
    double verticalDelta,
    String reason,
    String detail
) {
    public static ParkourJumpPlan rejected(String reason) {
        return rejected(reason, "");
    }

    public static ParkourJumpPlan rejected(String reason, String detail) {
        return new ParkourJumpPlan(false, 0, 0, 0.0, 0.0, 0.0, reason, detail);
    }
}
