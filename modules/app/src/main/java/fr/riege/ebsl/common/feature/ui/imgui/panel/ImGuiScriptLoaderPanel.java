package fr.riege.ebsl.common.feature.ui.imgui.panel;

import fr.riege.ebsl.common.feature.scripting.manager.EbslScriptManager;
import fr.riege.ebsl.common.feature.scripting.runtime.EbslScriptTask;
import fr.riege.ebsl.common.feature.ui.state.EbslUiState;
import fr.riege.ebsl.common.platform.EbslPlatform;
import imgui.ImGui;
import imgui.flag.ImGuiCol;

import java.util.List;

public final class ImGuiScriptLoaderPanel {
    private static final int RUNNING_TEXT = 0xFF52D273;
    private static final int RUNNING_HEADER = 0x5534B55F;
    private static final int RUNNING_HEADER_HOVERED = 0x7740C96F;
    private static final int RUNNING_HEADER_ACTIVE = 0x9948D97A;

    public void render(EbslUiState state, EbslPlatform platform) {
        EbslScriptManager manager = new EbslScriptManager(platform.storage());
        List<String> activeFiles = EbslScriptTask.INSTANCE.activeFiles();
        ImGui.text("Script loader");
        ImGui.separator();
        for (String script : manager.scripts()) {
            boolean selected = script.equals(state.selectedScriptFile());
            boolean running = activeFiles.contains(EbslScriptManager.normalizeFileName(script));
            if (running) {
                pushRunningScriptStyle();
            }
            if (ImGui.selectable(script, selected)) {
                state.selectScriptFile(script);
            }
            if (running) {
                ImGui.popStyleColor(4);
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

    private static void pushRunningScriptStyle() {
        ImGui.pushStyleColor(ImGuiCol.Text, RUNNING_TEXT);
        ImGui.pushStyleColor(ImGuiCol.Header, RUNNING_HEADER);
        ImGui.pushStyleColor(ImGuiCol.HeaderHovered, RUNNING_HEADER_HOVERED);
        ImGui.pushStyleColor(ImGuiCol.HeaderActive, RUNNING_HEADER_ACTIVE);
    }
}
