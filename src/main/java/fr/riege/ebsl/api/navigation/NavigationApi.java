package fr.riege.ebsl.api.navigation;

import fr.riege.ebsl.api.annotation.EbslApiOperation;
import fr.riege.ebsl.api.annotation.EbslApiSurface;
import fr.riege.ebsl.pathfinding.Node;
import fr.riege.ebsl.pathfinding.PathfinderConfig;
import fr.riege.ebsl.pathfinding.PathfindingManager;
import fr.riege.ebsl.pathfinding.debug.PathVisualizer;

@EbslApiSurface(EbslApiSurface.Domain.NAVIGATION)
public final class NavigationApi {
    public NavigationApi() {
    }

    @EbslApiOperation("Read the current navigation snapshot.")
    public NavigationSnapshot snapshot() {
        return new NavigationSnapshot(
            PathfindingManager.isNavigating(),
            PathfindingManager.getCurrentMoveType(),
            PathfindingManager.isWalkSneakLatched(),
            PathfinderConfig.PATHFINDER_MAX_JUMP_HEIGHT.get(),
            PathVisualizer.isEnabled()
        );
    }

    @EbslApiOperation("Stop active navigation.")
    public void stop() {
        PathfindingManager.stop();
    }

    @EbslApiOperation("Check whether navigation is active.")
    public boolean isNavigating() {
        return PathfindingManager.isNavigating();
    }

    @EbslApiOperation("Read the currently executed move type.")
    public Node.MoveType currentMoveType() {
        return PathfindingManager.getCurrentMoveType();
    }
}
