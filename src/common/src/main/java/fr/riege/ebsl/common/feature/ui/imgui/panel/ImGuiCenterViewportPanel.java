package fr.riege.ebsl.common.feature.ui.imgui.panel;

import fr.riege.ebsl.common.feature.ui.imgui.ImGuiPanelUtil;
import fr.riege.ebsl.common.feature.ui.layout.UiRect;
import fr.riege.ebsl.common.feature.ui.layout.UiTheme;
import fr.riege.ebsl.common.feature.ui.layout.ViewportLayout;
import fr.riege.ebsl.common.feature.ui.state.CenterTab;
import fr.riege.ebsl.common.feature.ui.state.EbslUiState;
import fr.riege.ebsl.common.feature.ui.state.MainViewTab;
import fr.riege.ebsl.common.platform.EbslPlatform;
import fr.riege.ebsl.common.platform.service.NavigationService;
import imgui.ImDrawList;
import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;

public final class ImGuiCenterViewportPanel implements ImGuiUiPanel {
    private final ImGuiPathfinderSettingsPanel pathfinderSettingsPanel = new ImGuiPathfinderSettingsPanel();
    private final ImGuiRenderSettingsPanel renderSettingsPanel = new ImGuiRenderSettingsPanel();
    private final ImGuiPacketPanel packetPanel = new ImGuiPacketPanel();
    private final ImGuiTerminalPanel terminalPanel = new ImGuiTerminalPanel();
    private final ImGuiMcLogPanel mcLogPanel = new ImGuiMcLogPanel();
    private final ImGuiScriptEditorPanel scriptEditorPanel = new ImGuiScriptEditorPanel();

    @Override
    public void render(EbslUiState state, ViewportLayout layout, NavigationService navigation, EbslPlatform platform) {
        ImGuiPanelUtil.nextFixedWindow(layout.center());
        int flags = ImGuiPanelUtil.FIXED_PANEL_FLAGS | ImGuiWindowFlags.NoBackground;
        if (ImGui.begin("##ebsl-center-viewport", flags)) {
            UiRect tabs = tabsRect(layout);
            UiRect viewport = viewportRect(layout);
            drawTabs(state, tabs);
            if (state.mainViewTab() == MainViewTab.SCRIPT) {
                scriptEditorPanel.render(state, viewport, platform);
            } else {
                renderSelectedTab(state.centerTab(), viewport);
            }
            drawViewportFrame(viewport);
            ImGui.end();
        }
    }

    private UiRect tabsRect(ViewportLayout layout) {
        UiRect c = layout.center();
        return new UiRect(c.x(), c.y(), c.width(), UiTheme.TAB_H + 8);
    }

    private UiRect viewportRect(ViewportLayout layout) {
        UiRect c = layout.center();
        int top = c.y() + UiTheme.TAB_H + 8;
        return new UiRect(c.x() + 8, top, c.width() - 16, c.bottom() - top - 8);
    }

    private void renderSelectedTab(CenterTab tab, UiRect viewport) {
        if (tab != CenterTab.TERMINAL) {
            terminalPanel.resetFocus();
        }
        switch (tab) {
            case GAME -> drawGameViewportShell(viewport);
            case PATHFINDER_SETTINGS -> pathfinderSettingsPanel.render(viewport);
            case RENDER_SETTINGS -> renderSettingsPanel.render(viewport);
            case PACKET -> packetPanel.render(viewport);
            case TERMINAL -> terminalPanel.render(viewport);
            case MC_LOG -> mcLogPanel.render(viewport);
        }
    }

    private void drawTabs(EbslUiState state, UiRect tabs) {
        ImDrawList dl = ImGui.getWindowDrawList();
        dl.addRectFilled(tabs.x(), tabs.y(), tabs.right(), tabs.bottom(), 0xEE10141A);
        ImGui.setCursorScreenPos(tabs.x() + 8.0f, tabs.y() + 5.0f);
        if (state.mainViewTab() == MainViewTab.SCRIPT) {
            ImGui.text("EBSL script editor");
            return;
        }
        for (CenterTab tab : CenterTab.values()) {
            if (ImGui.button(tab.label(), tabWidth(tab), 22.0f)) state.setCenterTab(tab);
            ImGui.sameLine();
        }
    }

    private static float tabWidth(CenterTab tab) {
        return switch (tab) {
            case GAME -> 72.0f;
            case PACKET -> 86.0f;
            case PATHFINDER_SETTINGS -> 148.0f;
            case RENDER_SETTINGS -> 78.0f;
            case TERMINAL -> 86.0f;
            case MC_LOG -> 72.0f;
        };
    }

    private void drawGameViewportShell(UiRect viewport) {
        ImGui.getWindowDrawList().addRectFilled(
            viewport.x(), viewport.y(), viewport.right(), viewport.bottom(), 0x18000000);
    }

    private static void drawViewportFrame(UiRect viewport) {
        ImDrawList dl = ImGui.getForegroundDrawList();
        dl.addRect(viewport.x(), viewport.y(), viewport.right(), viewport.bottom(), 0xFF67B7FF, 0.0f, 0, 2.0f);
        dl.addLine(viewport.x(), viewport.y(), viewport.right(), viewport.y(), 0xFFFFFFFF, 1.0f);
        dl.addLine(viewport.x(), viewport.bottom(), viewport.right(), viewport.bottom(), 0xAA67B7FF, 1.0f);
    }
}
