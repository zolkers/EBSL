package fr.riege.ebsl.common.feature.ui.state;

import fr.riege.ebsl.common.feature.module.BotModuleRegistry;
import fr.riege.ebsl.common.feature.module.PathfinderModule;
import fr.riege.ebsl.common.feature.task.BotTask;
import fr.riege.ebsl.common.feature.task.BotTaskRegistry;

public final class EbslUiState {
    private MainViewTab mainViewTab = MainViewTab.MAIN;
    private CenterTab centerTab = CenterTab.GAME;
    private RightPanelMode rightPanelMode = RightPanelMode.MODULE_LIST;
    private PathfinderModule selectedModule;
    private BotTask selectedTask;

    public MainViewTab mainViewTab()  { return mainViewTab; }
    public CenterTab centerTab()      { return centerTab; }
    public RightPanelMode rightPanelMode() { return rightPanelMode; }

    public void setMainViewTab(MainViewTab t) { mainViewTab = t; }
    public void setCenterTab(CenterTab t)     { centerTab = t; }

    public void showModuleList()                  { rightPanelMode = RightPanelMode.MODULE_LIST; }
    public void showTaskList()                    { rightPanelMode = RightPanelMode.TASK_LIST; }

    public void showModuleSettings(PathfinderModule m) {
        selectedModule = m;
        rightPanelMode = RightPanelMode.MODULE_SETTINGS;
    }

    public void showTaskSettings(BotTask t) {
        selectedTask = t;
        rightPanelMode = RightPanelMode.TASK_SETTINGS;
    }

    public PathfinderModule selectedModule() {
        if (selectedModule == null) {
            selectedModule = BotModuleRegistry.modules().stream().findFirst().orElse(null);
        } else {
            selectedModule = BotModuleRegistry.get(selectedModule.id());
        }
        return selectedModule;
    }

    public BotTask selectedTask() {
        if (selectedTask == null) {
            selectedTask = BotTaskRegistry.tasks().stream().findFirst().orElse(null);
        } else {
            selectedTask = BotTaskRegistry.get(selectedTask.id());
        }
        return selectedTask;
    }
}
