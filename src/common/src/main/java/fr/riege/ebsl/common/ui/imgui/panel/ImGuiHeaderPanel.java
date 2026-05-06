package fr.riege.ebsl.common.ui.imgui.panel;

import fr.riege.ebsl.common.service.NavigationService;
import fr.riege.ebsl.common.ui.imgui.ImGuiPanelUtil;
import fr.riege.ebsl.common.ui.layout.ViewportLayout;
import fr.riege.ebsl.common.ui.state.EbslUiState;
import fr.riege.ebsl.common.ui.state.MainViewTab;
import imgui.ImGui;

public final class ImGuiHeaderPanel implements ImGuiUiPanel {
    @Override
    public void render(EbslUiState state, ViewportLayout layout, NavigationService navigation) {
        ImGuiPanelUtil.nextFixedWindow(layout.header());
        if (ImGui.begin("##ebsl-header", ImGuiPanelUtil.FIXED_PANEL_FLAGS)) {
            ImGui.text("EBSL");
            ImGui.sameLine(72.0f);
            for (MainViewTab tab : MainViewTab.values()) {
                if (ImGui.button(tab.label(), 72.0f, 18.0f)) {
                    state.setMainViewTab(tab);
                }
                ImGui.sameLine();
            }
            ImGui.end();
        }
    }
}
