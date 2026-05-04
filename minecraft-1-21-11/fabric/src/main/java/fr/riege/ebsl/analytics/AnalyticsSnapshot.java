package fr.riege.ebsl.analytics;

import fr.riege.ebsl.api.EbslApi;
import fr.riege.ebsl.api.navigation.NavigationSnapshot;
import fr.riege.ebsl.general.module.PathfinderModule;

public record AnalyticsSnapshot(
    String navigationState,
    String selectedModule,
    int jumpHeight,
    boolean visualizerEnabled
) {
    public static AnalyticsSnapshot capture(PathfinderModule selectedModule) {
        NavigationSnapshot pathfinding = EbslApi.navigation().snapshot();
        return new AnalyticsSnapshot(
            pathfinding.navigationStateLabel(),
            selectedModule != null ? selectedModule.displayName() : "none",
            pathfinding.maxJumpHeight(),
            pathfinding.visualizerEnabled());
    }
}
