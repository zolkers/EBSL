/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Copyright (C) 2026 Victor Riegert and EBSL contributors
 *
 * This file is part of EBSL.
 *
 * EBSL is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EBSL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EBSL. If not, see <https://www.gnu.org/licenses/>.
 */

package fr.riege.ebsl.common.feature.ui.imgui.panel;

import fr.riege.ebsl.common.core.threading.EbslThreadError;
import fr.riege.ebsl.common.core.threading.EbslThreadErrorLog;
import fr.riege.ebsl.common.feature.ui.layout.UiRect;
import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;
import fr.riege.ebsl.common.platform.render.RenderBatch;
import fr.riege.ebsl.common.platform.render.RenderingSystem;
import imgui.ImGui;
import imgui.flag.ImGuiCond;

import java.util.List;

final class ImGuiRenderSettingsPanel {
    void render(UiRect viewport) {
        ImGuiPanelChrome.begin(viewport, "##render-settings-scroll");
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
