package fr.riege.ebsl.api.gui;

import fr.riege.ebsl.analytics.AnalyticsEvent;
import fr.riege.ebsl.analytics.AnalyticsSnapshot;
import fr.riege.ebsl.api.EbslApi;
import fr.riege.ebsl.api.annotation.EbslApiOperation;
import fr.riege.ebsl.api.annotation.EbslApiSurface;
import fr.riege.ebsl.general.module.PathfinderModule;
import fr.riege.ebsl.general.task.BotTask;
import fr.riege.ebsl.terminal.goal.GoalUiCatalog;
import fr.riege.ebsl.terminal.goal.GoalUiDefinition;

import java.util.List;

@EbslApiSurface(EbslApiSurface.Domain.UI)
public final class GuiApi {
    public GuiApi() {
    }

    @EbslApiOperation("Capture the data required to populate the ImGui interface.")
    public GuiSnapshot snapshot(PathfinderModule selectedModule, int eventCount) {
        return new GuiSnapshot(
            EbslApi.navigation().snapshot(),
            modules(),
            tasks(),
            pathfinderSettingsGroups(),
            goalDefinitions(),
            latestAnalyticsEvents(eventCount)
        );
    }

    @EbslApiOperation("Read all pathfinder modules for GUI rendering.")
    public List<PathfinderModule> modules() {
        return List.copyOf(EbslApi.modules().all());
    }

    @EbslApiOperation("Find the GUI-selected module by id.")
    public PathfinderModule module(String id) {
        return EbslApi.modules().get(id);
    }

    @EbslApiOperation("Read all bot tasks for GUI rendering.")
    public List<BotTask> tasks() {
        return List.copyOf(EbslApi.tasks().all());
    }

    @EbslApiOperation("Find the GUI-selected task by id.")
    public BotTask task(String id) {
        return EbslApi.tasks().get(id);
    }

    @EbslApiOperation("Read settings grouped for the pathfinder settings screen.")
    public List<GuiSettingsGroup> pathfinderSettingsGroups() {
        return List.of(
            new GuiSettingsGroup("General", EbslApi.settings().pathfinding().general()),
            new GuiSettingsGroup("Movement costs", EbslApi.settings().pathfinding().movementCosts()),
            new GuiSettingsGroup("Corridor and centering costs", EbslApi.settings().pathfinding().corridorCosts()),
            new GuiSettingsGroup("Planning limits", EbslApi.settings().pathfinding().planningLimits()),
            new GuiSettingsGroup("Smoothing and post-processing", EbslApi.settings().pathfinding().smoothing()),
            new GuiSettingsGroup("Execution", EbslApi.settings().pathfinding().execution()),
            new GuiSettingsGroup("Recovery", EbslApi.settings().pathfinding().recovery()),
            new GuiSettingsGroup("Path health checks", EbslApi.settings().pathfinding().pathChecks()),
            new GuiSettingsGroup("Execution steering", EbslApi.settings().pathfinding().steering()),
            new GuiSettingsGroup("Long range", EbslApi.settings().pathfinding().longRange()),
            new GuiSettingsGroup("Rotation and camera", EbslApi.settings().pathfinding().rotation())
        );
    }

    @EbslApiOperation("Read path goal definitions for GUI rendering.")
    public List<GoalUiDefinition> goalDefinitions() {
        return GoalUiCatalog.all();
    }

    @EbslApiOperation("Read the analytics summary used by the GUI.")
    public AnalyticsSnapshot analyticsSnapshot(PathfinderModule selectedModule) {
        return EbslApi.analytics().snapshot(selectedModule);
    }

    @EbslApiOperation("Read recent analytics events used by the GUI.")
    public List<AnalyticsEvent> latestAnalyticsEvents(int count) {
        return EbslApi.analytics().latestEvents(count);
    }
}
