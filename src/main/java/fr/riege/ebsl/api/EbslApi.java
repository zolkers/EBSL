package fr.riege.ebsl.api;

import fr.riege.ebsl.api.analytics.AnalyticsApi;
import fr.riege.ebsl.api.annotation.EbslApiOperation;
import fr.riege.ebsl.api.annotation.EbslApiSurface;
import fr.riege.ebsl.api.events.EventsApi;
import fr.riege.ebsl.api.gui.GuiApi;
import fr.riege.ebsl.api.modules.ModulesApi;
import fr.riege.ebsl.api.navigation.NavigationApi;
import fr.riege.ebsl.api.settings.ModSettingsApi;
import fr.riege.ebsl.api.tasks.TasksApi;

@EbslApiSurface(EbslApiSurface.Domain.CORE)
public final class EbslApi {
    private static final NavigationApi NAVIGATION = new NavigationApi();
    private static final ModSettingsApi SETTINGS = new ModSettingsApi();
    private static final ModulesApi MODULES = new ModulesApi();
    private static final TasksApi TASKS = new TasksApi();
    private static final EventsApi EVENTS = new EventsApi();
    private static final AnalyticsApi ANALYTICS = new AnalyticsApi();
    private static final GuiApi GUI = new GuiApi();

    private EbslApi() {
    }

    @EbslApiOperation("Access navigation state and commands.")
    public static NavigationApi navigation() {
        return NAVIGATION;
    }

    @EbslApiOperation("Access mod settings grouped by domain.")
    public static ModSettingsApi settings() {
        return SETTINGS;
    }

    @EbslApiOperation("Access bot/pathfinder modules.")
    public static ModulesApi modules() {
        return MODULES;
    }

    @EbslApiOperation("Access bot tasks.")
    public static TasksApi tasks() {
        return TASKS;
    }

    @EbslApiOperation("Access the client event bus.")
    public static EventsApi events() {
        return EVENTS;
    }

    @EbslApiOperation("Access analytics snapshots and event logs.")
    public static AnalyticsApi analytics() {
        return ANALYTICS;
    }

    @EbslApiOperation("Access UI-ready data models for ImGui and overlays.")
    public static GuiApi gui() {
        return GUI;
    }
}
