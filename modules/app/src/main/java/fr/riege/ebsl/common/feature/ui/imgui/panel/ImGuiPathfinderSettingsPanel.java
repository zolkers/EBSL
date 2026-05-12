package fr.riege.ebsl.common.feature.ui.imgui.panel;

import fr.riege.ebsl.common.feature.ui.layout.UiRect;
import fr.riege.ebsl.common.feature.ui.layout.UiTheme;
import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;
import imgui.ImDrawList;
import imgui.ImGui;

final class ImGuiPathfinderSettingsPanel {
    void render(UiRect viewport) {
        ImDrawList dl = ImGui.getWindowDrawList();
        dl.addRectFilled(viewport.x(), viewport.y(), viewport.right(), viewport.bottom(), UiTheme.BG_PANEL_DARK);
        dl.addRectFilled(viewport.x(), viewport.y(), viewport.right(), viewport.y() + 34.0f, UiTheme.BG_PANEL);
        dl.addLine(viewport.x(), viewport.y() + 34.0f, viewport.right(), viewport.y() + 34.0f, UiTheme.BORDER, 1.0f);
        ImGui.setCursorScreenPos(viewport.x() + 14.0f, viewport.y() + 14.0f);
        ImGui.beginChild("##pf-settings-scroll", viewport.width() - 28.0f, viewport.height() - 28.0f, false);
        ImGui.text("Pathfinder Settings");
        ImGui.sameLine();
        if (ImGui.button("Reset to defaults", 148.0f, 18.0f)) PathfinderSettings.resetToDefaults();
        ImGui.spacing();
        ImGuiSettingControls.renderGroup("General", PathfinderSettings.generalSettings());
        ImGuiSettingControls.renderGroup("Movement cost", PathfinderSettings.movementCostSettings());
        ImGuiSettingControls.renderGroup("Execution", PathfinderSettings.executionSettings());
        ImGuiSettingControls.renderGroup("Recovery", PathfinderSettings.recoverySettings());
        ImGuiSettingControls.renderGroup("Smoothing", PathfinderSettings.smoothingSettings());
        ImGui.endChild();
    }
}
