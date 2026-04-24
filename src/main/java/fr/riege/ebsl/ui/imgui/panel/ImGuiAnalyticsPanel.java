package fr.riege.ebsl.ui.imgui.panel;

import fr.riege.ebsl.analytics.AnalyticsEvent;
import fr.riege.ebsl.analytics.AnalyticsEventLog;
import fr.riege.ebsl.analytics.AnalyticsSnapshot;
import fr.riege.ebsl.ui.imgui.ImGuiPanelUtil;
import fr.riege.ebsl.ui.layout.ViewportLayout;
import fr.riege.ebsl.ui.state.EbslUiState;
import imgui.ImGui;

public final class ImGuiAnalyticsPanel implements ImGuiUiPanel {
    @Override
    public void render(EbslUiState state, ViewportLayout layout) {
        ImGuiPanelUtil.nextFixedWindow(layout.bottom());
        if (ImGui.begin("Analytics##ebsl-bottom", ImGuiPanelUtil.FIXED_PANEL_FLAGS)) {
            AnalyticsSnapshot snapshot = AnalyticsSnapshot.capture(state.selectedModule());
            ImGui.text("Analytics");
            ImGui.separator();
            ImGui.columns(2, "ebsl-analytics-columns", false);
            ImGui.textDisabled("Navigation: " + snapshot.navigationState());
            ImGui.textDisabled("Selected module: " + snapshot.selectedModule());
            ImGui.textDisabled("Jump height: " + snapshot.jumpHeight());
            ImGui.textDisabled("Visualizer: always on");
            ImGui.nextColumn();
            ImGui.text("Event log");
            for (AnalyticsEvent event : AnalyticsEventLog.latest(4)) {
                ImGui.textDisabled(event.source() + ": " + event.message());
            }
            ImGui.columns(1);
            ImGui.end();
        }
    }
}
