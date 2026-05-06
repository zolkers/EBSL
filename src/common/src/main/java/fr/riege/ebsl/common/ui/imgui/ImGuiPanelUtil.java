package fr.riege.ebsl.common.ui.imgui;

import fr.riege.ebsl.common.ui.layout.UiRect;
import imgui.ImDrawList;
import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;

public final class ImGuiPanelUtil {
    public static final int FIXED_PANEL_FLAGS =
        ImGuiWindowFlags.NoMove
            | ImGuiWindowFlags.NoResize
            | ImGuiWindowFlags.NoCollapse
            | ImGuiWindowFlags.NoSavedSettings
            | ImGuiWindowFlags.NoTitleBar;

    private ImGuiPanelUtil() {}

    public static void nextFixedWindow(UiRect rect) {
        ImGui.setNextWindowPos(rect.x(), rect.y(), ImGuiCond.Always);
        ImGui.setNextWindowSize(rect.width(), rect.height(), ImGuiCond.Always);
    }

    public static void drawRectBorder(UiRect rect, int color, float thickness) {
        ImDrawList dl = ImGui.getForegroundDrawList();
        dl.addRect(rect.x(), rect.y(), rect.right(), rect.bottom(), color, 0.0f, 0, thickness);
    }

    public static void drawLine(float x1, float y1, float x2, float y2, int color, float thickness) {
        ImGui.getForegroundDrawList().addLine(x1, y1, x2, y2, color, thickness);
    }
}
