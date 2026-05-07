package fr.riege.ebsl.common.feature.ui.imgui.panel;

import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeCategory;
import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeType;
import fr.riege.ebsl.common.feature.scripting.manager.EbslNodeTemplate;
import fr.riege.ebsl.common.feature.ui.imgui.ImGuiPanelUtil;
import fr.riege.ebsl.common.feature.ui.layout.UiRect;
import fr.riege.ebsl.common.feature.ui.state.EbslUiState;
import imgui.ImGui;
import imgui.type.ImString;

public final class ImGuiScriptNodePalettePanel {
    private final ImString filter = new ImString("", 64);

    public void render(EbslUiState state, UiRect rect) {
        ImGuiPanelUtil.nextFixedWindow(rect);
        if (ImGui.begin("EBSL nodes##ebsl-right-script-nodes", ImGuiPanelUtil.FIXED_PANEL_FLAGS)) {
            ImGui.text("Node palette");
            ImGui.setNextItemWidth(-1.0f);
            ImGui.inputText("##ebsl-node-palette-filter", filter);
            ImGui.separator();
            for (EbslNodeCategory category : EbslNodeCategory.values()) {
                if (ImGui.collapsingHeader(category.id())) {
                    for (EbslNodeType type : EbslNodeType.values()) {
                        EbslNodeTemplate template = EbslNodeTemplate.of(type);
                        if (type.category() == category && type.executable() && template.matches(filter.get())) {
                            if (ImGui.selectable(template.title() + "##" + type.id(), false)) {
                                state.requestScriptInsert(template.sampleLine());
                            }
                            ImGui.textDisabled(template.command() + "  " + template.argsHint());
                        }
                    }
                }
            }
            ImGui.end();
        }
    }
}
