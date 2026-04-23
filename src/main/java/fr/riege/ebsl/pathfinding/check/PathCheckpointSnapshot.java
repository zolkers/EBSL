package fr.riege.ebsl.pathfinding.check;

public record PathCheckpointSnapshot(
    int checkpointIndex,
    int projectedSegmentIndex,
    int skippedCheckpoints,
    double distanceToNextCheckpoint,
    long staleDurationMs
) {
}
