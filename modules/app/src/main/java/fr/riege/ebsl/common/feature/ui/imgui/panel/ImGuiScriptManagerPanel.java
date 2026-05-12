package fr.riege.ebsl.common.feature.ui.imgui.panel;

import fr.riege.ebsl.common.feature.scripting.manager.EbslScriptManager;
import fr.riege.ebsl.common.feature.ui.imgui.ImGuiPanelUtil;
import fr.riege.ebsl.common.feature.ui.layout.UiRect;
import fr.riege.ebsl.common.feature.ui.state.EbslUiState;
import fr.riege.ebsl.common.platform.EbslPlatform;
import imgui.ImGui;
import imgui.type.ImString;
import java.util.List;

public final class ImGuiScriptManagerPanel {
    private final ImString newFileName = new ImString(EbslScriptManager.stripExtension(EbslScriptManager.DEFAULT_FILE), 96);

    public void render(EbslUiState state, UiRect rect, EbslPlatform platform) {
        EbslScriptManager manager = new EbslScriptManager(platform.storage());
        ImGuiPanelUtil.nextFixedWindow(rect);
        if (ImGui.begin("EBSL scripts##ebsl-left-scripts", ImGuiPanelUtil.FIXED_PANEL_FLAGS)) {
            ImGui.text("Scripts");
            ImGui.separator();
            if (ImGui.beginChild("##ebsl-script-list", 0.0f, 220.0f, true)) {
                for (String script : manager.scripts()) {
                    boolean selected = script.equals(state.selectedScriptFile());
                    if (ImGui.selectable(script, selected)) {
                        state.selectScriptFile(script);
                    }
                }
                ImGui.endChild();
            }
            ImGui.separator();
            ImGui.inputText("Name", newFileName);
            if (ImGui.button("Create", -1.0f, 24.0f)) {
                String file = EbslScriptManager.normalizeFileName(newFileName.get());
                manager.create(file);
                state.selectScriptFile(file);
                newFileName.set(EbslScriptManager.stripExtension(file));
            }
            if (ImGui.button("Delete selected", -1.0f, 24.0f)) {
                manager.delete(state.selectedScriptFile());
                List<String> scripts = manager.scripts();
                if (scripts.isEmpty()) {
                    manager.create(EbslScriptManager.DEFAULT_FILE);
                    state.selectScriptFile(EbslScriptManager.DEFAULT_FILE);
                } else {
                    state.selectScriptFile(scripts.getFirst());
                }
            }
            ImGui.spacing();
            ImGui.textDisabled("Editing only. Run scripts from Main > Scripts.");
            ImGui.end();
        }
    }
}
