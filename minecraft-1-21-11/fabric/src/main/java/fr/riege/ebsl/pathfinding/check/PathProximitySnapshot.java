package fr.riege.ebsl.pathfinding.check;

public record PathProximitySnapshot(
    int nearestSegmentIndex,
    int nearestNodeIndex,
    double segmentT,
    double horizontalDistance,
    double verticalDistance,
    double distance3d,
    double progress
) {
}
