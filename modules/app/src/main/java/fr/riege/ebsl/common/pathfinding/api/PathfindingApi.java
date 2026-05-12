package fr.riege.ebsl.common.pathfinding.api;

import fr.riege.ebsl.common.api.EbslApi;
import fr.riege.ebsl.common.api.pathfinding.PathfindingSnapshot;

public final class PathfindingApi {
    private PathfindingApi() {
    }

    public static PathfindingSnapshot snapshot() {
        return EbslApi.pathfinding().snapshot();
    }

    public static void stop() {
        EbslApi.pathfinding().stop();
    }
}
