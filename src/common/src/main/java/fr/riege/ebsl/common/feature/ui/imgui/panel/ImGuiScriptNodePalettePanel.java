package fr.riege.ebsl.common.feature.ui.imgui.panel;

import fr.riege.ebsl.common.feature.scripting.EbslNode;
import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeCategory;
import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeType;
import fr.riege.ebsl.common.feature.scripting.manager.EbslNodeTemplate;
import fr.riege.ebsl.common.feature.scripting.registry.EbslNodeRegistry;
import fr.riege.ebsl.common.feature.ui.imgui.EbslNodeCategoryColors;
import fr.riege.ebsl.common.feature.ui.imgui.ImGuiPanelUtil;
import fr.riege.ebsl.common.feature.ui.layout.UiRect;
import fr.riege.ebsl.common.feature.ui.state.EbslUiState;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.type.ImString;

import java.util.ArrayList;
import java.util.List;

public final class ImGuiScriptNodePalettePanel {
    private static final List<EbslNodeType> SCRIPT_BLOCKS = List.of(
        EbslNodeType.EVENT_FUNCTION,
        EbslNodeType.CONTROL_IF,
        EbslNodeType.CONTROL_IF_ELSE,
        EbslNodeType.CONTROL_REPEAT,
        EbslNodeType.CONTROL_REPEAT_UNTIL,
        EbslNodeType.CONTROL_FOREVER
    );

    private final ImString filter = new ImString("", 64);

    public void render(EbslUiState state, UiRect rect) {
        ImGuiPanelUtil.nextFixedWindow(rect);
        if (ImGui.begin("EBSL nodes##ebsl-right-script-nodes", ImGuiPanelUtil.FIXED_PANEL_FLAGS)) {
            ImGui.text("Node palette");
            ImGui.setNextItemWidth(-1.0f);
            ImGui.inputText("##ebsl-node-palette-filter", filter);
            ImGui.separator();
            for (EbslNodeCategory category : EbslNodeCategory.values()) {
                pushCategoryColors(category);
                if (ImGui.collapsingHeader(category.id())) {
                    for (EbslNodeTemplate template : templates()) {
                        if (template.category() == category && template.matches(filter.get())) {
                            if (ImGui.selectable(template.title() + "##" + template.command(), false)) {
                                state.requestScriptInsert(template.sampleLine());
                            }
                            ImGui.textDisabled(template.command() + "  " + template.argsHint());
                        }
                    }
                }
                ImGui.popStyleColor(4);
            }
            ImGui.end();
        }
    }

    private List<EbslNodeTemplate> templates() {
        List<EbslNodeTemplate> templates = new ArrayList<>();
        for (EbslNode node : EbslNodeRegistry.canonicalNodes()) {
            templates.add(EbslNodeTemplate.of(node));
        }
        for (EbslNodeType block : SCRIPT_BLOCKS) {
            templates.add(EbslNodeTemplate.of(block));
        }
        return templates;
    }

    private void pushCategoryColors(EbslNodeCategory category) {
        ImGui.pushStyleColor(ImGuiCol.Header, EbslNodeCategoryColors.header(category));
        ImGui.pushStyleColor(ImGuiCol.HeaderHovered, EbslNodeCategoryColors.headerHovered(category));
        ImGui.pushStyleColor(ImGuiCol.HeaderActive, EbslNodeCategoryColors.headerHovered(category));
        ImGui.pushStyleColor(ImGuiCol.Text, EbslNodeCategoryColors.text(category));
    }
}
