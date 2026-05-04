package fr.riege.ebsl.ui.imgui.panel;

import fr.riege.ebsl.ui.imgui.ImGuiPanelUtil;
import fr.riege.ebsl.ui.layout.ViewportLayout;
import fr.riege.ebsl.ui.state.EbslUiState;
import fr.riege.ebsl.ui.state.MainViewTab;
import imgui.ImGui;

public final class ImGuiHeaderPanel implements ImGuiUiPanel {
    @Override
    public void render(EbslUiState state, ViewportLayout layout) {
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
