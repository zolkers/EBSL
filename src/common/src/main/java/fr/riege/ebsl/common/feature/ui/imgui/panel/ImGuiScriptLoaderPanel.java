package fr.riege.ebsl.common.feature.ui.imgui.panel;

import fr.riege.ebsl.common.feature.scripting.manager.EbslScriptManager;
import fr.riege.ebsl.common.feature.scripting.runtime.EbslScriptTask;
import fr.riege.ebsl.common.feature.ui.state.EbslUiState;
import fr.riege.ebsl.common.platform.EbslPlatform;
import imgui.ImGui;

public final class ImGuiScriptLoaderPanel {
    public void render(EbslUiState state, EbslPlatform platform) {
        EbslScriptManager manager = new EbslScriptManager(platform.storage());
        ImGui.text("Script loader");
        ImGui.separator();
        for (String script : manager.scripts()) {
            boolean selected = script.equals(state.selectedScriptFile());
            if (ImGui.selectable(script, selected)) {
                state.selectScriptFile(script);
            }
        }
        ImGui.separator();
        ImGui.textDisabled("Selected: " + state.selectedScriptFile());
        if (ImGui.button("Load into task", -1.0f, 24.0f)) {
            EbslScriptTask.INSTANCE.loadFile(state.selectedScriptFile());
        }
        if (ImGui.button("Run now", -1.0f, 24.0f)) {
            EbslScriptTask.INSTANCE.runFile(state.selectedScriptFile());
        }
        if (ImGui.button("Stop selected", -1.0f, 24.0f)) {
            EbslScriptTask.INSTANCE.stop(state.selectedScriptFile());
        }
        if (ImGui.button("Stop all", -1.0f, 24.0f)) {
            EbslScriptTask.INSTANCE.stopAll();
        }
        ImGui.textDisabled("Task status: " + EbslScriptTask.INSTANCE.status());
        for (String line : EbslScriptTask.INSTANCE.activeLines()) {
            ImGui.textDisabled(line);
        }
    }
}
