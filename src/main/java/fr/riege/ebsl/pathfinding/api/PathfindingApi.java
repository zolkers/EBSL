package fr.riege.ebsl.pathfinding.api;

import fr.riege.ebsl.api.EbslApi;
import fr.riege.ebsl.api.navigation.NavigationSnapshot;

public final class PathfindingApi {
    private PathfindingApi() {
    }

    public static PathfindingSnapshot snapshot() {
        NavigationSnapshot snapshot = EbslApi.navigation().snapshot();
        return new PathfindingSnapshot(
            snapshot.navigating(),
            snapshot.currentMoveType(),
            snapshot.walkSneakLatched(),
            snapshot.maxJumpHeight(),
            snapshot.visualizerEnabled());
    }

    public static void stop() {
        EbslApi.navigation().stop();
    }
}
