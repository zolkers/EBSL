package fr.riege.ebsl.api.gui;

import fr.riege.ebsl.analytics.AnalyticsEvent;
import fr.riege.ebsl.analytics.AnalyticsSnapshot;
import fr.riege.ebsl.api.EbslApi;
import fr.riege.ebsl.api.annotation.EbslApiOperation;
import fr.riege.ebsl.api.annotation.EbslApiSurface;
import fr.riege.ebsl.botting.module.PathfinderModule;
import fr.riege.ebsl.command.GoalCommands;
import fr.riege.ebsl.command.GoalRegistry;
import fr.riege.ebsl.command.goal.GoalUiDefinition;

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

    @EbslApiOperation("Read settings grouped for the pathfinder settings screen.")
    public List<GuiSettingsGroup> pathfinderSettingsGroups() {
        return List.of(
            new GuiSettingsGroup("General", EbslApi.settings().pathfinding().general()),
            new GuiSettingsGroup("Movement costs", EbslApi.settings().pathfinding().movementCosts()),
            new GuiSettingsGroup("Corridor and centering costs", EbslApi.settings().pathfinding().corridorCosts())
        );
    }

    @EbslApiOperation("Read path goal definitions for GUI rendering.")
    public List<GoalUiDefinition> goalDefinitions() {
        GoalCommands.bootstrap();
        return GoalRegistry.uiDefinitions();
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
