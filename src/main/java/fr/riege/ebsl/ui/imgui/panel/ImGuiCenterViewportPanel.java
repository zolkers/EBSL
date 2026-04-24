package fr.riege.ebsl.ui.imgui.panel;

import fr.riege.ebsl.ui.imgui.ImGuiPanelUtil;
import fr.riege.ebsl.packet.PacketCaptureEvent;
import fr.riege.ebsl.packet.PacketCaptureLog;
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
import imgui.type.ImString;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class ImGuiCenterViewportPanel implements ImGuiUiPanel {
    private static final DateTimeFormatter PACKET_TIME_FORMAT =
        DateTimeFormatter.ofPattern("HH:mm:ss.SSS").withZone(ZoneId.systemDefault());
    private final ImString packetFilter = new ImString(64);

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
            } else if (state.centerTab() == CenterTab.PATHFINDER_SETTINGS) {
                renderPathfinderSettings(viewport);
            } else {
                renderPacketView(viewport);
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
            float width = switch (tab) {
                case GAME -> 72.0f;
                case PACKET -> 86.0f;
                case PATHFINDER_SETTINGS -> 148.0f;
            };
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

    private void renderPacketView(UiRect viewport) {
        ImDrawList drawList = ImGui.getWindowDrawList();
        drawList.addRectFilled(viewport.x(), viewport.y(), viewport.right(), viewport.bottom(), 0xEE101820);
        ImGui.setCursorScreenPos(viewport.x() + 14.0f, viewport.y() + 14.0f);
        ImGui.beginGroup();
        ImGui.text("Packet");
        ImGui.sameLine();
        ImGui.textDisabled("C++ proxy-style packet log, captured in-client");

        ImBoolean enabled = new ImBoolean(PacketCaptureLog.isEnabled());
        if (ImGui.checkbox("Capture", enabled)) {
            PacketCaptureLog.setEnabled(enabled.get());
        }
        ImGui.sameLine();
        if (ImGui.button("Clear", 72.0f, 22.0f)) {
            PacketCaptureLog.clear();
        }
        ImGui.sameLine();
        ImGui.textDisabled("Inbound: " + PacketCaptureLog.inboundCount()
            + " | Outbound: " + PacketCaptureLog.outboundCount());

        ImGui.inputText("Filter", packetFilter);
        ImGui.separator();
        renderPacketRows(viewport);
        ImGui.endGroup();
    }

    private void renderPacketRows(UiRect viewport) {
        List<PacketCaptureEvent> events = PacketCaptureLog.snapshot();
        String filter = packetFilter.get().trim().toLowerCase();
        float listHeight = Math.max(80.0f, viewport.height() - 112.0f);
        if (ImGui.beginChild("##packet-log", viewport.width() - 28.0f, listHeight, true)) {
            int rendered = 0;
            for (int i = events.size() - 1; i >= 0 && rendered < 350; i--) {
                PacketCaptureEvent event = events.get(i);
                if (!matchesPacketFilter(event, filter)) {
                    continue;
                }
                ImGui.textDisabled(formatPacketEvent(event));
                rendered++;
            }
            if (rendered == 0) {
                ImGui.textDisabled("No packets captured yet.");
            }
            ImGui.endChild();
        }
    }

    private static boolean matchesPacketFilter(PacketCaptureEvent event, String filter) {
        if (filter.isBlank()) {
            return true;
        }
        return event.packetId().toLowerCase().contains(filter)
            || event.packetClass().toLowerCase().contains(filter)
            || event.direction().label().toLowerCase().contains(filter);
    }

    private static String formatPacketEvent(PacketCaptureEvent event) {
        String flags = (event.terminal() ? " terminal" : "") + (event.skippable() ? " skippable" : "");
        return "#" + event.sequence()
            + " " + PACKET_TIME_FORMAT.format(Instant.ofEpochMilli(event.capturedAtMs()))
            + " " + event.direction().label()
            + " " + event.packetId()
            + " [" + event.packetClass() + "]"
            + flags;
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
