package fr.riege.ebsl.common.feature.ui.imgui.panel;

import fr.riege.ebsl.common.feature.ui.layout.UiRect;
import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;
import imgui.ImGui;

final class ImGuiPathfinderSettingsPanel {
    void render(UiRect viewport) {
        ImGuiPanelChrome.begin(viewport, "##pf-settings-scroll");
        ImGui.text("Pathfinder Settings");
        ImGui.sameLine();
        if (ImGui.button("Reset to defaults", 148.0f, 18.0f)) PathfinderSettings.resetToDefaults();
        ImGui.spacing();
        ImGuiSettingControls.renderGroup("General", PathfinderSettings.generalSettings());
        ImGuiSettingControls.renderGroup("Movement cost", PathfinderSettings.movementCostSettings());
        ImGuiSettingControls.renderGroup("Path quality", PathfinderSettings.qualitySettings());
        ImGuiSettingControls.renderGroup("Execution", PathfinderSettings.executionSettings());
        ImGuiSettingControls.renderGroup("Recovery", PathfinderSettings.recoverySettings());
        ImGuiSettingControls.renderGroup("Smoothing", PathfinderSettings.smoothingSettings());
        ImGui.endChild();
    }
}
