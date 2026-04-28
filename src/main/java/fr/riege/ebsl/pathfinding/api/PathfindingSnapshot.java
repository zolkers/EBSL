package fr.riege.ebsl.pathfinding.api;

import fr.riege.ebsl.pathfinding.Node;

public record PathfindingSnapshot(
    boolean navigating,
    Node.MoveType currentMoveType,
    boolean walkSneakLatched,
    int maxJumpHeight,
    boolean visualizerEnabled
) {
    public String navigationStateLabel() {
        return navigating ? "navigating" : "idle";
    }
}
