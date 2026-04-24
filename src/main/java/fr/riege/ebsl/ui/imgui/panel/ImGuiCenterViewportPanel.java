package fr.riege.ebsl.ui.imgui.panel;

import fr.riege.ebsl.ui.imgui.ImGuiPanelUtil;
import fr.riege.ebsl.pathfinding.settings.PathfinderSettings;
import fr.riege.ebsl.pathfinding.settings.PathfinderSettingsStore;
import fr.riege.ebsl.settings.BooleanSetting;
import fr.riege.ebsl.settings.IntSetting;
import fr.riege.ebsl.settings.Setting;
import fr.riege.ebsl.ui.layout.UiRect;
import fr.riege.ebsl.ui.layout.UiTheme;
import fr.riege.ebsl.ui.layout.ViewportLayout;
import fr.riege.ebsl.ui.state.CenterTab;
import fr.riege.ebsl.ui.state.EbslUiState;
import imgui.ImGui;
import imgui.ImDrawList;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;

public final class ImGuiCenterViewportPanel implements ImGuiUiPanel {
    @Override
    public void render(EbslUiState state, ViewportLayout layout) {
        ImGuiPanelUtil.nextFixedWindow(layout.center());
        int flags = ImGuiPanelUtil.FIXED_PANEL_FLAGS | ImGuiWindowFlags.NoBackground;
        if (ImGui.begin("##ebsl-center-viewport", flags)) {
            UiRect tabs = tabsRect(layout);
            UiRect viewport = viewportRect(layout);
            drawTabs(state, tabs);
            if (state.centerTab() == CenterTab.GAME) {
                drawGameViewportShell(viewport);
            } else {
                renderPathfinderSettings(viewport);
            }
            drawViewportFrame(viewport);
            ImGui.end();
        }
    }

    private UiRect tabsRect(ViewportLayout layout) {
        UiRect center = layout.center();
        return new UiRect(center.x(), center.y(), center.width(), UiTheme.TAB_H + 8);
    }

    private UiRect viewportRect(ViewportLayout layout) {
        UiRect center = layout.center();
        int top = center.y() + UiTheme.TAB_H + 8;
        return new UiRect(center.x() + 8, top, center.width() - 16, center.bottom() - top - 8);
    }

    private void drawTabs(EbslUiState state, UiRect tabs) {
        ImDrawList drawList = ImGui.getWindowDrawList();
        drawList.addRectFilled(tabs.x(), tabs.y(), tabs.right(), tabs.bottom(), 0xEE10141A);
        ImGui.setCursorScreenPos(tabs.x() + 8.0f, tabs.y() + 5.0f);
        for (CenterTab tab : CenterTab.values()) {
            float width = tab == CenterTab.GAME ? 72.0f : 148.0f;
            if (ImGui.button(tab.label(), width, 22.0f)) {
                state.setCenterTab(tab);
            }
            ImGui.sameLine();
        }
    }

    private void renderPathfinderSettings(UiRect viewport) {
        ImDrawList drawList = ImGui.getWindowDrawList();
        drawList.addRectFilled(viewport.x(), viewport.y(), viewport.right(), viewport.bottom(), 0xEE143B66);
        ImGui.setCursorScreenPos(viewport.x() + 18.0f, viewport.y() + 18.0f);
        ImGui.beginGroup();
        ImGui.text("Pathfinder Settings");
        ImGui.textDisabled("These settings replace the whole viewport.");
        ImGui.spacing();
        for (Setting<?> setting : PathfinderSettings.all()) {
            if (setting instanceof BooleanSetting boolSetting) {
                ImBoolean value = new ImBoolean(boolSetting.value());
                if (ImGui.checkbox(setting.displayName(), value)) {
                    boolSetting.setValue(value.get());
                    PathfinderSettings.apply();
                    PathfinderSettingsStore.save();
                }
            } else if (setting instanceof IntSetting intSetting) {
                int[] value = {intSetting.value()};
                if (ImGui.sliderInt(setting.displayName(), value, intSetting.min(), intSetting.max())) {
                    intSetting.setValue(value[0]);
                    PathfinderSettings.apply();
                    PathfinderSettingsStore.save();
                }
            }
        }
        ImGui.spacing();
        if (ImGui.button("Reset to defaults", 148.0f, 24.0f)) {
            PathfinderSettings.resetToDefaults();
            PathfinderSettingsStore.save();
        }
        ImGui.endGroup();
    }

    private void drawGameViewportShell(UiRect viewport) {
        ImDrawList drawList = ImGui.getWindowDrawList();
        drawList.addRectFilled(viewport.x(), viewport.y(), viewport.right(), viewport.bottom(), 0x18000000);
    }

    private void drawViewportFrame(UiRect viewport) {
        ImDrawList drawList = ImGui.getForegroundDrawList();
        drawList.addRect(viewport.x(), viewport.y(), viewport.right(), viewport.bottom(), 0xFF67B7FF, 0.0f, 0, 2.0f);
        drawList.addLine(viewport.x(), viewport.y(), viewport.right(), viewport.y(), 0xFFFFFFFF, 1.0f);
        drawList.addLine(viewport.x(), viewport.bottom(), viewport.right(), viewport.bottom(), 0xAA67B7FF, 1.0f);
    }
}
