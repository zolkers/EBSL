package fr.riege.ebsl.api.gui;

import fr.riege.ebsl.analytics.AnalyticsEvent;
import fr.riege.ebsl.api.navigation.NavigationSnapshot;
import fr.riege.ebsl.botting.module.PathfinderModule;
import fr.riege.ebsl.command.goal.GoalUiDefinition;

import java.util.List;

public record GuiSnapshot(
    NavigationSnapshot navigation,
    List<PathfinderModule> modules,
    List<GuiSettingsGroup> pathfinderSettings,
    List<GoalUiDefinition> goals,
    List<AnalyticsEvent> analyticsEvents
) {
}
