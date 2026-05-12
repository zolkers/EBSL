package fr.riege.ebsl.common.feature.ui.state;

import fr.riege.ebsl.common.feature.module.BotModuleRegistry;
import fr.riege.ebsl.common.feature.module.PathfinderModule;
import fr.riege.ebsl.common.feature.scripting.manager.EbslScriptManager;
import fr.riege.ebsl.common.feature.scripting.manager.EbslScriptView;
import fr.riege.ebsl.common.feature.task.BotTask;
import fr.riege.ebsl.common.feature.task.BotTaskRegistry;

public final class EbslUiState {
    private MainViewTab mainViewTab = MainViewTab.MAIN;
    private CenterTab centerTab = CenterTab.GAME;
    private RightPanelMode rightPanelMode = RightPanelMode.MODULE_LIST;
    private PathfinderModule selectedModule;
    private BotTask selectedTask;
    private EbslScriptView scriptView = EbslScriptView.GRAPH;
    private String selectedScriptFile = EbslScriptManager.DEFAULT_FILE;
    private String pendingScriptInsert = "";
    private int scriptRevision;

    public MainViewTab mainViewTab()  { return mainViewTab; }
    public CenterTab centerTab()      { return centerTab; }
    public RightPanelMode rightPanelMode() { return rightPanelMode; }
    public EbslScriptView scriptView() { return scriptView; }
    public String selectedScriptFile() { return selectedScriptFile; }
    public int scriptRevision() { return scriptRevision; }

    public void setMainViewTab(MainViewTab t) { mainViewTab = t; }
    public void setCenterTab(CenterTab t)     { centerTab = t; }
    public void setScriptView(EbslScriptView view) { scriptView = view; }
    public void selectScriptFile(String file) {
        selectedScriptFile = EbslScriptManager.normalizeFileName(file);
        scriptRevision++;
    }
    public void requestScriptInsert(String line) { pendingScriptInsert = line == null ? "" : line; }

    public String consumeScriptInsert() {
        String insert = pendingScriptInsert;
        pendingScriptInsert = "";
        return insert;
    }

    public void showModuleList()                  { rightPanelMode = RightPanelMode.MODULE_LIST; }
    public void showTaskList()                    { rightPanelMode = RightPanelMode.TASK_LIST; }
    public void showScriptLoader()                { rightPanelMode = RightPanelMode.SCRIPT_LOADER; }

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
