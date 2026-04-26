package fr.riege.ebsl.ui.state;

import fr.riege.ebsl.botting.module.PathfinderModule;
import fr.riege.ebsl.botting.registry.BotModuleRegistry;

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
            selectedModule = BotModuleRegistry.modules().stream().findFirst().orElse(null);
        } else {
            selectedModule = BotModuleRegistry.get(selectedModule.id());
        }
        return selectedModule;
    }
}
