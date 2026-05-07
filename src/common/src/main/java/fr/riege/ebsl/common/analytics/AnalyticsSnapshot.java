package fr.riege.ebsl.common.analytics;

import fr.riege.ebsl.common.module.PathfinderModule;
import fr.riege.ebsl.common.navigation.NavigationStatus;
import fr.riege.ebsl.common.service.NavigationService;

public record AnalyticsSnapshot(
    NavigationStatus navigationState,
    String selectedModule,
    int jumpHeight,
    boolean visualizerEnabled
) {
    public static AnalyticsSnapshot capture(NavigationService nav, PathfinderModule selectedModule) {
        return new AnalyticsSnapshot(
            nav.pathStatus(),
            selectedModule != null ? selectedModule.displayName() : "none",
            0,
            false);
    }
}
