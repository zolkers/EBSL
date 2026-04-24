package fr.riege.ebsl.pathfinding.execution;

import fr.riege.ebsl.pathfinding.check.PathProximitySnapshot;

public record PathProgressSnapshot(
    double distanceMoved,
    long movementStaleMs,
    long pathStaleMs,
    PathProximitySnapshot proximity
) {
    public boolean movementStale(long thresholdMs) {
        return movementStaleMs > thresholdMs;
    }

    public boolean pathStale(long thresholdMs) {
        return pathStaleMs > thresholdMs;
    }

    public boolean drifted(double threshold) {
        return proximity.horizontalDistance() > threshold;
    }
}
