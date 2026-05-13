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

import fr.riege.ebsl.common.core.log.AppLog;
import fr.riege.ebsl.common.core.log.AppLogLevel;
import fr.riege.ebsl.common.feature.ui.layout.UiRect;
import imgui.ImDrawList;
import imgui.ImGui;

import java.util.List;

final class ImGuiMcLogPanel {
    private boolean scrollToBottom;

    void render(UiRect viewport) {
        ImDrawList dl = ImGui.getWindowDrawList();
        dl.addRectFilled(viewport.x(), viewport.y(), viewport.right(), viewport.bottom(), 0xEE0A0F14);

        final float padX = 8.0f;
        final float clearW = 44.0f;
        final float headerH = 30.0f;

        dl.addRectFilled(viewport.x(), viewport.y(), viewport.right(), viewport.y() + headerH, 0xEE131921);
        ImGui.setCursorScreenPos(viewport.x() + padX, viewport.y() + 6.0f);
        ImGui.textColored(0.60f, 0.72f, 0.90f, 1.0f, "Game Log");
        ImGui.setCursorScreenPos(viewport.right() - clearW - 6.0f, viewport.y() + 5.0f);
        if (ImGui.button("Clear##mclog", clearW, 20.0f)) AppLog.clear();

        float listY = viewport.y() + headerH + 4.0f;
        float listH = viewport.height() - headerH - 8.0f;
        ImGui.setCursorScreenPos(viewport.x() + padX, listY);
        if (ImGui.beginChild("##mclog-scroll", viewport.width() - padX * 2, listH, false)) {
            if (AppLog.consumeDirty()) scrollToBottom = true;
            List<AppLog.LogEntry> entries = AppLog.snapshot();
            for (AppLog.LogEntry entry : entries) {
                renderEntry(entry);
            }
            if (entries.isEmpty()) ImGui.textDisabled("No log entries yet.");
            if (scrollToBottom) {
                ImGui.setScrollHereY(1.0f);
                scrollToBottom = false;
            }
            ImGui.endChild();
        }
    }

    private static void renderEntry(AppLog.LogEntry entry) {
        float[] col = levelColor(entry.level());
        ImGui.textColored(0.35f, 0.42f, 0.52f, 1.0f, entry.time());
        ImGui.sameLine();
        ImGui.textColored(col[0], col[1], col[2], 1.0f, "[" + entry.level().label() + "]");
        ImGui.sameLine();
        ImGui.textColored(0.50f, 0.60f, 0.72f, 1.0f, entry.logger() + ":");
        ImGui.sameLine();
        ImGui.textColored(0.85f, 0.90f, 0.95f, 1.0f, entry.text());
    }

    private static float[] levelColor(AppLogLevel level) {
        return switch (level) {
            case ERROR, FATAL -> new float[]{0.90f, 0.30f, 0.25f};
            case WARN -> new float[]{0.95f, 0.72f, 0.20f};
            case DEBUG, TRACE -> new float[]{0.42f, 0.52f, 0.62f};
            default -> new float[]{0.55f, 0.85f, 0.55f};
        };
    }
}
