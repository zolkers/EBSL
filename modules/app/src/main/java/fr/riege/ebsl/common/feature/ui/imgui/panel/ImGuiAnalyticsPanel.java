package fr.riege.ebsl.common.feature.ui.imgui.panel;

import fr.riege.ebsl.common.domain.analytics.AnalyticsEvent;
import fr.riege.ebsl.common.domain.analytics.AnalyticsEventLog;
import fr.riege.ebsl.common.domain.analytics.AnalyticsSnapshot;
import fr.riege.ebsl.common.platform.service.NavigationService;
import fr.riege.ebsl.common.feature.ui.imgui.ImGuiPanelUtil;
import fr.riege.ebsl.common.feature.ui.layout.ViewportLayout;
import fr.riege.ebsl.common.feature.ui.state.EbslUiState;
import fr.riege.ebsl.common.feature.ui.state.MainViewTab;
import fr.riege.ebsl.common.platform.EbslPlatform;
import imgui.ImGui;

public final class ImGuiAnalyticsPanel implements ImGuiUiPanel {
    @Override
    public void render(EbslUiState state, ViewportLayout layout, NavigationService navigation, EbslPlatform platform) {
        if (state.mainViewTab() != MainViewTab.MAIN) {
            return;
        }
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
