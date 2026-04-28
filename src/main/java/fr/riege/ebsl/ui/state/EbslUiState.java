package fr.riege.ebsl.ui.state;

import fr.riege.ebsl.api.EbslApi;
import fr.riege.ebsl.botting.module.PathfinderModule;

public final class EbslUiState {
    private MainViewTab mainViewTab = MainViewTab.MAIN;
    private CenterTab centerTab = CenterTab.GAME;
    private RightPanelMode rightPanelMode = RightPanelMode.MODULE_LIST;
    private PathfinderModule selectedModule;

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

    public void showModuleSettings(PathfinderModule module) {
        selectedModule = module;
        rightPanelMode = RightPanelMode.MODULE_SETTINGS;
    }

    public PathfinderModule selectedModule() {
        if (selectedModule == null) {
            selectedModule = EbslApi.modules().all().stream().findFirst().orElse(null);
        } else {
            selectedModule = EbslApi.modules().get(selectedModule.id());
        }
        return selectedModule;
    }
}
