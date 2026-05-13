/*
 * This file is part of EBSL.
 *
 * EBSL is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EBSL is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package fr.riege.ebsl.common.feature.ui.imgui.panel;

import fr.riege.ebsl.common.feature.scripting.EbslNode;
import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeCategory;
import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeType;
import fr.riege.ebsl.common.feature.scripting.manager.EbslNodeFieldHelp;
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
                renderCategory(state, category);
            }
            ImGui.end();
        }
    }

    private void renderCategory(EbslUiState state, EbslNodeCategory category) {
        pushCategoryColors(category);
        if (ImGui.collapsingHeader(category.id())) {
            renderCategoryTemplates(state, category);
        }
        ImGui.popStyleColor(4);
    }

    private void renderCategoryTemplates(EbslUiState state, EbslNodeCategory category) {
        for (EbslNodeTemplate template : templates()) {
            if (template.category() == category && template.matches(filter.get())) {
                renderTemplate(state, template);
            }
        }
    }

    private static void renderTemplate(EbslUiState state, EbslNodeTemplate template) {
        if (ImGui.selectable(template.title() + "##" + template.command(), false)) {
            state.requestScriptInsert(template.sampleLine());
        }
        String signature = EbslNodeFieldHelp.signature(template.command());
        ImGui.textDisabled(signature.isBlank()
            ? template.command()
            : template.command() + "  " + signature);
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
