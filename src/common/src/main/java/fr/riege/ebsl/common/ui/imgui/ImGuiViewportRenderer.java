package fr.riege.ebsl.common.ui.imgui;

import fr.riege.ebsl.common.service.NavigationService;
import fr.riege.ebsl.common.ui.imgui.panel.*;
import fr.riege.ebsl.common.ui.layout.UiRect;
import fr.riege.ebsl.common.ui.layout.ViewportLayout;
import fr.riege.ebsl.common.ui.state.EbslUiState;
import imgui.ImGui;
import imgui.ImGuiStyle;

import java.util.List;

public final class ImGuiViewportRenderer {
    private final List<ImGuiUiPanel> panels = List.of(
        new ImGuiHeaderPanel(),
        new ImGuiGoalsPanel(),
        new ImGuiCenterViewportPanel(),
        new ImGuiModulesPanel(),
        new ImGuiAnalyticsPanel());
    private boolean styleApplied;

    public void render(EbslUiState state, ViewportLayout layout, NavigationService navigation) {
        applyStyleOnce();
        for (ImGuiUiPanel panel : panels) {
            panel.render(state, layout, navigation);
        }
        drawLayoutLines(layout);
    }

    private void drawLayoutLines(ViewportLayout layout) {
        int strong = 0xFF4A90E2;
        int soft   = 0xAA26313D;
        ImGuiPanelUtil.drawRectBorder(layout.center(), strong, 2.0f);
        ImGuiPanelUtil.drawRectBorder(layout.left(),   soft,   1.0f);
        ImGuiPanelUtil.drawRectBorder(layout.right(),  soft,   1.0f);
        ImGuiPanelUtil.drawRectBorder(layout.bottom(), soft,   1.0f);
        UiRect center = layout.center();
        ImGuiPanelUtil.drawLine(center.x(), center.y(), center.right(), center.y(), strong, 2.0f);
        ImGuiPanelUtil.drawLine(center.x(), center.bottom(), center.right(), center.bottom(), strong, 2.0f);
    }

    private void applyStyleOnce() {
        if (styleApplied) return;
        styleApplied = true;
        ImGuiStyle style = ImGui.getStyle();
        style.setWindowRounding(0.0f);
        style.setChildRounding(0.0f);
        style.setFrameRounding(3.0f);
        style.setGrabRounding(3.0f);
        style.setWindowBorderSize(0.0f);
        style.setFrameBorderSize(0.0f);
        style.setWindowPadding(8.0f, 8.0f);
        style.setItemSpacing(8.0f, 6.0f);
    }
}
