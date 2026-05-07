package fr.riege.ebsl.common.api.pathfinding;

import fr.riege.ebsl.common.navigation.NavigationStatus;
import fr.riege.ebsl.common.pathfinding.Node;

public record PathfindingSnapshot(
    NavigationStatus status,
    boolean navigating,
    Node.MoveType currentMoveType,
    boolean walkSneakLatched,
    int pathNodeCount
) {
    public String navigationStateLabel() {
        return status == null ? "unknown" : status.name().toLowerCase();
    }
}
