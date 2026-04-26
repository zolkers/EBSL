package fr.riege.ebsl.ui.imgui.panel;

import fr.riege.ebsl.EbslMod;
import fr.riege.ebsl.event.events.render.RenderGameViewportEvent;
import fr.riege.ebsl.ui.imgui.ImGuiPanelUtil;
import fr.riege.ebsl.packet.PacketCaptureEvent;
import fr.riege.ebsl.packet.PacketCaptureLog;
import fr.riege.ebsl.pathfinding.settings.PathfinderSettings;
import fr.riege.ebsl.pathfinding.settings.PathfinderSettingsStore;
import fr.riege.ebsl.settings.BooleanSetting;
import fr.riege.ebsl.settings.DoubleSetting;
import fr.riege.ebsl.settings.IntSetting;
import fr.riege.ebsl.settings.Setting;
import fr.riege.ebsl.ui.layout.UiRect;
import fr.riege.ebsl.ui.layout.UiTheme;
import fr.riege.ebsl.ui.layout.ViewportLayout;
import fr.riege.ebsl.ui.state.CenterTab;
import fr.riege.ebsl.ui.state.EbslUiState;
import imgui.ImGui;
import imgui.ImDrawList;
import imgui.flag.ImGuiCond;
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
        drawList.addRectFilled(viewport.x(), viewport.y(), viewport.right(), viewport.bottom(), UiTheme.BG_PANEL_DARK);
        drawList.addRectFilled(viewport.x(), viewport.y(), viewport.right(), viewport.y() + 34.0f, UiTheme.BG_PANEL);
        drawList.addLine(viewport.x(), viewport.y() + 34.0f, viewport.right(), viewport.y() + 34.0f, UiTheme.BORDER, 1.0f);
        drawList.addLine(viewport.x() + 1.0f, viewport.y(), viewport.x() + 1.0f, viewport.bottom(), 0x66364755, 1.0f);
        ImGui.setCursorScreenPos(viewport.x() + 14.0f, viewport.y() + 14.0f);
        ImGui.beginChild("##pathfinder-settings-scroll", viewport.width() - 28.0f, viewport.height() - 28.0f, false);
        ImGui.text("Pathfinder Settings");
        ImGui.sameLine();
        if (ImGui.button("Reset to defaults", 148.0f, 18.0f)) {
            PathfinderSettings.resetToDefaults();
            PathfinderSettingsStore.save();
        }
        ImGui.spacing();
        renderSettingsCategory("General", PathfinderSettings.generalSettings());
        renderSettingsCategory("Movement costs", PathfinderSettings.movementCostSettings());
        renderSettingsCategory("Corridor and centering costs", PathfinderSettings.corridorCostSettings());
        ImGui.endChild();
    }

    private void renderSettingsCategory(String label, List<Setting<?>> settings) {
        ImGui.setNextItemOpen(true, ImGuiCond.Once);
        if (!ImGui.collapsingHeader(label)) {
            return;
        }
        ImGui.indent(10.0f);
        for (Setting<?> setting : settings) {
            renderPathfinderSetting(setting);
        }
        ImGui.unindent(10.0f);
        ImGui.spacing();
    }

    private void renderPathfinderSetting(Setting<?> setting) {
        if (setting instanceof BooleanSetting boolSetting) {
            ImBoolean value = new ImBoolean(boolSetting.value());
            if (ImGui.checkbox(setting.displayName(), value)) {
                boolSetting.setValue(value.get());
                savePathfinderSettings();
            }
        } else if (setting instanceof IntSetting intSetting) {
            int[] value = {intSetting.value()};
            if (ImGui.sliderInt(setting.displayName(), value, intSetting.min(), intSetting.max())) {
                intSetting.setValue(value[0]);
                savePathfinderSettings();
            }
        } else if (setting instanceof DoubleSetting doubleSetting) {
            float[] value = {(float) doubleSetting.value().doubleValue()};
            if (ImGui.sliderFloat(setting.displayName(), value, (float) doubleSetting.min(), (float) doubleSetting.max(), "%.2f")) {
                doubleSetting.setValue((double) value[0]);
                savePathfinderSettings();
            }
        }
    }

    private void savePathfinderSettings() {
        PathfinderSettings.apply();
        PathfinderSettingsStore.save();
    }

    private void renderPacketView(UiRect viewport) {
        ImDrawList drawList = ImGui.getWindowDrawList();
        drawList.addRectFilled(viewport.x(), viewport.y(), viewport.right(), viewport.bottom(), 0xEE101820);
        ImGui.setCursorScreenPos(viewport.x() + 14.0f, viewport.y() + 14.0f);
        ImGui.beginGroup();
        ImGui.text("Packet");

        ImBoolean enabled = new ImBoolean(PacketCaptureLog.isEnabled());
        if (ImGui.checkbox("Capture", enabled)) {
            PacketCaptureLog.setEnabled(enabled.get());
        }
        ImGui.sameLine();
        ImBoolean captureInbound = new ImBoolean(PacketCaptureLog.isCaptureInbound());
        if (ImGui.checkbox("S2C", captureInbound)) {
            PacketCaptureLog.setCaptureInbound(captureInbound.get());
        }
        ImGui.sameLine();
        ImBoolean captureOutbound = new ImBoolean(PacketCaptureLog.isCaptureOutbound());
        if (ImGui.checkbox("C2S", captureOutbound)) {
            PacketCaptureLog.setCaptureOutbound(captureOutbound.get());
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
        EbslMod.postClientEvent(new RenderGameViewportEvent(ImGui.getForegroundDrawList(), viewport));
    }

    private void drawViewportFrame(UiRect viewport) {
        ImDrawList drawList = ImGui.getForegroundDrawList();
        drawList.addRect(viewport.x(), viewport.y(), viewport.right(), viewport.bottom(), 0xFF67B7FF, 0.0f, 0, 2.0f);
        drawList.addLine(viewport.x(), viewport.y(), viewport.right(), viewport.y(), 0xFFFFFFFF, 1.0f);
        drawList.addLine(viewport.x(), viewport.bottom(), viewport.right(), viewport.bottom(), 0xAA67B7FF, 1.0f);
    }
}
