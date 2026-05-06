package fr.riege.ebsl.common.ui.imgui.panel;

import fr.riege.ebsl.common.analytics.AnalyticsEvent;
import fr.riege.ebsl.common.analytics.AnalyticsEventLog;
import fr.riege.ebsl.common.analytics.AnalyticsSnapshot;
import fr.riege.ebsl.common.service.NavigationService;
import fr.riege.ebsl.common.ui.imgui.ImGuiPanelUtil;
import fr.riege.ebsl.common.ui.layout.ViewportLayout;
import fr.riege.ebsl.common.ui.state.EbslUiState;
import imgui.ImGui;

public final class ImGuiAnalyticsPanel implements ImGuiUiPanel {
    @Override
    public void render(EbslUiState state, ViewportLayout layout, NavigationService navigation) {
        ImGuiPanelUtil.nextFixedWindow(layout.bottom());
        if (ImGui.begin("Analytics##ebsl-bottom", ImGuiPanelUtil.FIXED_PANEL_FLAGS)) {
            AnalyticsSnapshot snapshot = AnalyticsSnapshot.capture(navigation, state.selectedModule());
            ImGui.text("Analytics");
            ImGui.separator();
            ImGui.columns(2, "ebsl-analytics-columns", false);
            ImGui.textDisabled("Navigation: " + snapshot.navigationState());
            ImGui.textDisabled("Selected module: " + snapshot.selectedModule());
            ImGui.textDisabled("Jump height: " + snapshot.jumpHeight());
            ImGui.textDisabled("Visualizer: always on");
            ImGui.nextColumn();
            ImGui.text("Event log");
            for (AnalyticsEvent event : AnalyticsEventLog.latest(12)) {
                ImGui.textDisabled(event.source() + ": " + event.message());
            }
            ImGui.columns(1);
            ImGui.end();
        }
    }
}
