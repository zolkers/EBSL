package fr.riege.ebsl.analytics;

import fr.riege.ebsl.botting.module.BotModule;
import fr.riege.ebsl.pathfinding.PathfinderConfig;
import fr.riege.ebsl.pathfinding.PathfindingManager;

public record AnalyticsSnapshot(
    String navigationState,
    String selectedModule,
    int jumpHeight,
    boolean visualizerEnabled
) {
    public static AnalyticsSnapshot capture(BotModule selectedModule) {
        return new AnalyticsSnapshot(
            PathfindingManager.isNavigating() ? "navigating" : "idle",
            selectedModule != null ? selectedModule.displayName() : "none",
            PathfinderConfig.PATHFINDER_MAX_JUMP_HEIGHT.get(),
            true);
    }
}
