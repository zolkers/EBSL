package fr.riege.ebsl.common.feature.ui.imgui.panel;

import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeCategory;
import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeType;
import fr.riege.ebsl.common.feature.ui.imgui.ImGuiPanelUtil;
import fr.riege.ebsl.common.feature.ui.layout.UiRect;
import imgui.ImGui;

public final class ImGuiScriptNodePalettePanel {
    public void render(UiRect rect) {
        ImGuiPanelUtil.nextFixedWindow(rect);
        if (ImGui.begin("EBSL nodes##ebsl-right-script-nodes", ImGuiPanelUtil.FIXED_PANEL_FLAGS)) {
            ImGui.text("Node palette");
            ImGui.separator();
            for (EbslNodeCategory category : EbslNodeCategory.values()) {
                if (ImGui.collapsingHeader(category.id())) {
                    for (EbslNodeType type : EbslNodeType.values()) {
                        if (type.category() == category && type.executable()) {
                            ImGui.selectable(type.id(), false);
                        }
                    }
                }
            }
            ImGui.end();
        }
    }
}
