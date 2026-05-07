package fr.riege.ebsl.common.api;

import fr.riege.ebsl.common.api.domain.analytics.AnalyticsApi;
import fr.riege.ebsl.common.api.core.annotation.EbslApiOperation;
import fr.riege.ebsl.common.api.core.annotation.EbslApiSurface;
import fr.riege.ebsl.common.api.core.events.EventsApi;
import fr.riege.ebsl.common.api.feature.modules.ModulesApi;
import fr.riege.ebsl.common.api.navigation.NavigationApi;
import fr.riege.ebsl.common.api.pathfinding.PathfindingApi;
import fr.riege.ebsl.common.api.rendering.RenderingApi;
import fr.riege.ebsl.common.api.threading.ThreadingApi;
import fr.riege.ebsl.common.api.navigation.runtime.RuntimeApi;
import fr.riege.ebsl.common.api.core.settings.ModSettingsApi;

@EbslApiSurface(EbslApiSurface.Domain.CORE)
public final class EbslApi {
    private static final NavigationApi NAVIGATION = new NavigationApi();
    private static final PathfindingApi PATHFINDING = new PathfindingApi();
    private static final RuntimeApi RUNTIME = new RuntimeApi();
    private static final ModSettingsApi SETTINGS = new ModSettingsApi();
    private static final ModulesApi MODULES = new ModulesApi();
    private static final EventsApi EVENTS = new EventsApi();
    private static final AnalyticsApi ANALYTICS = new AnalyticsApi();
    private static final RenderingApi RENDERING = new RenderingApi();
    private static final ThreadingApi THREADING = new ThreadingApi();

    private EbslApi() {
    }

    @EbslApiOperation("Access navigation state and commands.")
    public static NavigationApi navigation() {
        return NAVIGATION;
    }

    @EbslApiOperation("Access low-level path planning utilities.")
    public static PathfindingApi pathfinding() {
        return PATHFINDING;
    }

    @EbslApiOperation("Create runtime adapters for headless and server-side integrations.")
    public static RuntimeApi runtime() {
        return RUNTIME;
    }

    @EbslApiOperation("Access settings grouped by domain.")
    public static ModSettingsApi settings() {
        return SETTINGS;
    }

    @EbslApiOperation("Access pathfinder modules.")
    public static ModulesApi modules() {
        return MODULES;
    }

    @EbslApiOperation("Access the platform event bus.")
    public static EventsApi events() {
        return EVENTS;
    }

    @EbslApiOperation("Access analytics snapshots and event logs.")
    public static AnalyticsApi analytics() {
        return ANALYTICS;
    }

    @EbslApiOperation("Access world rendering commands and styles.")
    public static RenderingApi rendering() {
        return RENDERING;
    }

    @EbslApiOperation("Access EBSL managed executors and thread errors.")
    public static ThreadingApi threading() {
        return THREADING;
    }
}
