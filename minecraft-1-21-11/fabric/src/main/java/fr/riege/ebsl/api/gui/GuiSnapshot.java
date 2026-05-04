package fr.riege.ebsl.api.gui;

import fr.riege.ebsl.analytics.AnalyticsEvent;
import fr.riege.ebsl.api.navigation.NavigationSnapshot;
import fr.riege.ebsl.general.module.PathfinderModule;
import fr.riege.ebsl.general.task.BotTask;
import fr.riege.ebsl.terminal.goal.GoalUiDefinition;

import java.util.List;

public record GuiSnapshot(
    NavigationSnapshot navigation,
    List<PathfinderModule> modules,
    List<BotTask> tasks,
    List<GuiSettingsGroup> pathfinderSettings,
    List<GoalUiDefinition> goals,
    List<AnalyticsEvent> analyticsEvents
) {
}
