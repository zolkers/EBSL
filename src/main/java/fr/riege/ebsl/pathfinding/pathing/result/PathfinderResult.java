package fr.riege.ebsl.pathfinding.pathing.result;

public interface PathfinderResult {
    boolean successful();
    boolean hasFailed();
    boolean hasFallenBack();
    PathState getPathState();
    Path getPath();
}
