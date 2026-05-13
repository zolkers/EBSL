/*
 * This file is part of EBSL.
 *
 * EBSL is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EBSL is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package fr.riege.ebsl.common.feature.ui.imgui.panel;

import fr.riege.ebsl.common.domain.packet.PacketCaptureEvent;
import fr.riege.ebsl.common.domain.packet.PacketCaptureLog;
import fr.riege.ebsl.common.feature.ui.layout.UiRect;
import imgui.ImDrawList;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImString;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

final class ImGuiPacketPanel {
    private static final DateTimeFormatter PACKET_TIME =
        DateTimeFormatter.ofPattern("HH:mm:ss.SSS").withZone(ZoneId.systemDefault());

    private final ImString packetFilter = new ImString(64);

    void render(UiRect viewport) {
        ImDrawList dl = ImGui.getWindowDrawList();
        dl.addRectFilled(viewport.x(), viewport.y(), viewport.right(), viewport.bottom(), 0xEE101820);
        ImGui.setCursorScreenPos(viewport.x() + 14.0f, viewport.y() + 14.0f);
        ImGui.beginGroup();
        ImGui.text("Packet");

        ImBoolean enabled = new ImBoolean(PacketCaptureLog.isEnabled());
        if (ImGui.checkbox("Capture", enabled)) PacketCaptureLog.setEnabled(enabled.get());
        ImGui.sameLine();
        ImBoolean inbound = new ImBoolean(PacketCaptureLog.isCaptureInbound());
        if (ImGui.checkbox("S2C", inbound)) PacketCaptureLog.setCaptureInbound(inbound.get());
        ImGui.sameLine();
        ImBoolean outbound = new ImBoolean(PacketCaptureLog.isCaptureOutbound());
        if (ImGui.checkbox("C2S", outbound)) PacketCaptureLog.setCaptureOutbound(outbound.get());
        ImGui.sameLine();
        if (ImGui.button("Clear", 72.0f, 22.0f)) PacketCaptureLog.clear();
        ImGui.sameLine();
        ImGui.textDisabled("Inbound: " + PacketCaptureLog.inboundCount()
            + " | Outbound: " + PacketCaptureLog.outboundCount());

        ImGui.inputText("Filter", packetFilter);
        ImGui.separator();
        renderRows(viewport);
        ImGui.endGroup();
    }

    private void renderRows(UiRect viewport) {
        List<PacketCaptureEvent> events = PacketCaptureLog.snapshot();
        String filter = packetFilter.get().trim().toLowerCase();
        float listH = Math.max(80.0f, viewport.height() - 112.0f);
        if (ImGui.beginChild("##packet-log", viewport.width() - 28.0f, listH, true)) {
            int rendered = 0;
            int index = events.size() - 1;
            while (index >= 0 && rendered < 350) {
                PacketCaptureEvent event = events.get(index);
                index--;
                if (!matchesFilter(event, filter)) {
                    continue;
                }
                ImGui.textDisabled(formatPacket(event));
                rendered++;
            }
            if (rendered == 0) ImGui.textDisabled("No packets captured yet.");
            ImGui.endChild();
        }
    }

    private static boolean matchesFilter(PacketCaptureEvent event, String filter) {
        if (filter.isBlank()) return true;
        return event.packetId().toLowerCase().contains(filter)
            || event.packetClass().toLowerCase().contains(filter)
            || event.direction().label().toLowerCase().contains(filter);
    }

    private static String formatPacket(PacketCaptureEvent event) {
        String flags = (event.terminal() ? " terminal" : "") + (event.skippable() ? " skippable" : "");
        return "#" + event.sequence()
            + " " + PACKET_TIME.format(Instant.ofEpochMilli(event.capturedAtMs()))
            + " " + event.direction().label()
            + " " + event.packetId()
            + " [" + event.packetClass() + "]"
            + flags;
    }
}
