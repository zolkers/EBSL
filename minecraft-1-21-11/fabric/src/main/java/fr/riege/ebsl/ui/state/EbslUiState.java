package fr.riege.ebsl.ui.state;

import fr.riege.ebsl.api.EbslApi;
import fr.riege.ebsl.general.module.PathfinderModule;
import fr.riege.ebsl.general.task.BotTask;

public final class EbslUiState {
    private MainViewTab mainViewTab = MainViewTab.MAIN;
    private CenterTab centerTab = CenterTab.GAME;
    private RightPanelMode rightPanelMode = RightPanelMode.MODULE_LIST;
    private PathfinderModule selectedModule;
    private BotTask selectedTask;

    public MainViewTab mainViewTab() {
        return mainViewTab;
    }

    public void setMainViewTab(MainViewTab mainViewTab) {
        this.mainViewTab = mainViewTab;
    }

    public CenterTab centerTab() {
        return centerTab;
    }

    public void setCenterTab(CenterTab centerTab) {
        this.centerTab = centerTab;
    }

    public RightPanelMode rightPanelMode() {
        return rightPanelMode;
    }

    public void showModuleList() {
        rightPanelMode = RightPanelMode.MODULE_LIST;
    }

    public void showTaskList() {
        rightPanelMode = RightPanelMode.TASK_LIST;
    }

    public void showModuleSettings(PathfinderModule module) {
        selectedModule = module;
        rightPanelMode = RightPanelMode.MODULE_SETTINGS;
    }

    public void showTaskSettings(BotTask task) {
        selectedTask = task;
        rightPanelMode = RightPanelMode.TASK_SETTINGS;
    }

    public PathfinderModule selectedModule() {
        if (selectedModule == null) {
            selectedModule = EbslApi.gui().modules().stream().findFirst().orElse(null);
        } else {
            selectedModule = EbslApi.gui().module(selectedModule.id());
        }
        return selectedModule;
    }

    public BotTask selectedTask() {
        if (selectedTask == null) {
            selectedTask = EbslApi.gui().tasks().stream().findFirst().orElse(null);
        } else {
            selectedTask = EbslApi.gui().task(selectedTask.id());
        }
        return selectedTask;
    }
}
