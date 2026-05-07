package fr.riege.ebsl.common.feature.ui.imgui.panel;

import fr.riege.ebsl.common.core.threading.EbslThreadError;
import fr.riege.ebsl.common.core.threading.EbslThreadErrorLog;
import fr.riege.ebsl.common.feature.ui.layout.UiRect;
import fr.riege.ebsl.common.feature.ui.layout.UiTheme;
import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;
import fr.riege.ebsl.common.platform.render.RenderBatch;
import fr.riege.ebsl.common.platform.render.RenderingSystem;
import imgui.ImDrawList;
import imgui.ImGui;
import imgui.flag.ImGuiCond;

import java.util.List;

final class ImGuiRenderSettingsPanel {
    void render(UiRect viewport) {
        ImDrawList dl = ImGui.getWindowDrawList();
        dl.addRectFilled(viewport.x(), viewport.y(), viewport.right(), viewport.bottom(), UiTheme.BG_PANEL_DARK);
        dl.addRectFilled(viewport.x(), viewport.y(), viewport.right(), viewport.y() + 34.0f, UiTheme.BG_PANEL);
        dl.addLine(viewport.x(), viewport.y() + 34.0f, viewport.right(), viewport.y() + 34.0f, UiTheme.BORDER, 1.0f);
        ImGui.setCursorScreenPos(viewport.x() + 14.0f, viewport.y() + 14.0f);
        ImGui.beginChild("##render-settings-scroll", viewport.width() - 28.0f, viewport.height() - 28.0f, false);
        ImGui.text("Render Settings");
        ImGui.sameLine();
        if (ImGui.button("Reset render", 112.0f, 18.0f)) {
            ImGuiSettingControls.reset(PathfinderSettings.renderingSettings());
        }
        ImGui.sameLine();
        if (ImGui.button("Clear batches", 112.0f, 18.0f)) RenderingSystem.clear();
        ImGui.sameLine();
        if (ImGui.button("Clear thread errors", 142.0f, 18.0f)) EbslThreadErrorLog.clear();
        ImGui.spacing();
        renderRuntime();
        renderThreadErrors();
        ImGuiSettingControls.renderGroup("Path visualizer", PathfinderSettings.renderingSettings());
        ImGui.endChild();
    }

    private void renderRuntime() {
        List<RenderBatch> batches = RenderingSystem.batches();
        ImGui.setNextItemOpen(true, ImGuiCond.Once);
        if (!ImGui.collapsingHeader("Runtime")) return;
        ImGui.indent(10.0f);
        ImGui.textDisabled("API batches: " + batches.size());
        int shown = 0;
        for (RenderBatch batch : batches) {
            if (shown >= 8) {
                ImGui.textDisabled("... +" + (batches.size() - shown) + " more");
                break;
            }
            ImGui.textDisabled(batch.id() + " | " + batch.stage() + " | primitives: " + batch.primitives().size());
            shown++;
        }
        ImGui.unindent(10.0f);
        ImGui.spacing();
    }

    private void renderThreadErrors() {
        List<EbslThreadError> errors = EbslThreadErrorLog.snapshot();
        ImGui.setNextItemOpen(false, ImGuiCond.Once);
        if (!ImGui.collapsingHeader("Thread errors")) return;
        ImGui.indent(10.0f);
        if (errors.isEmpty()) {
            ImGui.textDisabled("No thread errors captured.");
        } else {
            int shown = 0;
            for (int i = errors.size() - 1; i >= 0 && shown < 10; i--, shown++) {
                EbslThreadError error = errors.get(i);
                ImGui.textColored(0.90f, 0.35f, 0.30f, 1.0f,
                    "#" + error.sequence() + " " + error.domain().id() + " " + error.owner());
                ImGui.textDisabled(error.threadName() + " | " + error.exceptionClass());
                if (!error.message().isBlank()) {
                    ImGui.textDisabled(error.message());
                }
            }
        }
        ImGui.unindent(10.0f);
        ImGui.spacing();
    }
}
