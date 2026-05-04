package fr.riege.ebsl.api.navigation;

import fr.riege.ebsl.pathfinding.Node;

public record NavigationSnapshot(
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
