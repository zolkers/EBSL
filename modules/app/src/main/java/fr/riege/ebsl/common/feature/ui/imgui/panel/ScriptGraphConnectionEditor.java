/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Copyright (C) 2026 Victor Riegert and EBSL contributors
 *
 * This file is part of EBSL.
 *
 * EBSL is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EBSL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EBSL. If not, see <https://www.gnu.org/licenses/>.
 */

package fr.riege.ebsl.common.feature.ui.imgui.panel;

import fr.riege.ebsl.common.feature.scripting.manager.EbslGraphConnection;
import fr.riege.ebsl.common.feature.scripting.manager.EbslGraphConnectionMode;
import imgui.ImDrawList;
import imgui.ImGui;
import imgui.type.ImInt;
import imgui.type.ImString;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;
import java.util.function.UnaryOperator;

final class ScriptGraphConnectionEditor {
    private final List<EbslGraphConnection> connections = new ArrayList<>();
    private final ImString selectedLabel = new ImString("", 128);
    private final ImInt selectedMode = new ImInt(0);
    private String connectingFromKey = "";
    private String hoveredInputKey = "";
    private String selectedConnectionId = "";

    List<EbslGraphConnection> connections() {
        return List.copyOf(connections);
    }

    void load(List<EbslGraphConnection> connections) {
        this.connections.clear();
        this.connections.addAll(connections);
        connectingFromKey = "";
        hoveredInputKey = "";
        clearSelection();
    }

    boolean drawEdges(ImDrawList dl, List<ScriptGraphNodeLayout> layouts,
                      Map<String, ScriptGraphNodeLayout> layoutByKey, float graphZoom) {
        connections.removeIf(connection -> !layoutByKey.containsKey(connection.fromKey()) || !layoutByKey.containsKey(connection.toKey()));
        boolean selected = false;
        for (EbslGraphConnection connection : connections) {
            ScriptGraphNodeLayout from = layoutByKey.get(connection.fromKey());
            ScriptGraphNodeLayout to = layoutByKey.get(connection.toKey());
            if (from != null && to != null) {
                GraphEdgePainter.draw(dl, from, to, layouts, graphZoom, connection.id().equals(selectedConnectionId));
                if (handleEdgeHit(connection, from, to, graphZoom)) {
                    selected = true;
                }
            }
        }
        hoveredInputKey = "";
        return selected;
    }

    void handlePorts(ScriptGraphNodeLayout layout, float graphZoom) {
        float hit = Math.max(14.0f, 16.0f * graphZoom);
        float inputX = layout.x() + 10.0f * graphZoom;
        float outputX = layout.right() - 10.0f * graphZoom;
        float portY = layout.centerY();

        ImGui.setCursorScreenPos(inputX - hit * 0.5f, portY - hit * 0.5f);
        ImGui.invisibleButton("##ebsl-graph-input-" + layout.index(), hit, hit);
        if (ImGui.isItemHovered()) {
            hoveredInputKey = layout.node().key();
        }
        if (ImGui.isItemClicked()) {
            selectFirstIncoming(layout.node().key());
        }

        ImGui.setCursorScreenPos(outputX - hit * 0.5f, portY - hit * 0.5f);
        ImGui.invisibleButton("##ebsl-graph-output-" + layout.index(), hit, hit);
        if (ImGui.isItemClicked()) {
            connectingFromKey = layout.node().key();
            clearSelection();
        }
    }

    void finishDrag(ImDrawList dl, Map<String, ScriptGraphNodeLayout> layouts,
                    float graphZoom, Runnable save, Consumer<String> statusSink) {
        if (connectingFromKey.isBlank()) {
            return;
        }
        ScriptGraphNodeLayout from = layouts.get(connectingFromKey);
        if (from != null) {
            float startX = from.right() - 10.0f * graphZoom;
            dl.addLine(startX, from.centerY(), ImGui.getMousePosX(), ImGui.getMousePosY(), 0xDD67B7FF, 2.0f);
        }
        if (!ImGui.isMouseReleased(0)) {
            return;
        }
        if (!hoveredInputKey.isBlank() && !hoveredInputKey.equals(connectingFromKey)) {
            addConnection(connectingFromKey, hoveredInputKey, save, statusSink);
        }
        connectingFromKey = "";
    }

    void detachNode(String key, Runnable save, Consumer<String> statusSink) {
        if (connections.removeIf(connection -> connection.touches(key))) {
            clearSelection();
            save.run();
            statusSink.accept("detached node");
        }
    }

    void removeNode(String key) {
        connections.removeIf(connection -> connection.touches(key));
        clearSelection();
    }

    void clearSelection() {
        selectedConnectionId = "";
        selectedLabel.set("");
        selectedMode.set(0);
    }

    boolean hasSelectedConnection() {
        return selectedConnection() != null;
    }

    void renderSelectedConnectionInspector(Runnable save, Consumer<String> statusSink, float width) {
        EbslGraphConnection connection = selectedConnection();
        if (connection == null) {
            ImGui.text("Link inspector");
            ImGui.textDisabled("Select a link to edit its execution mode.");
            return;
        }

        ImGui.text("Link inspector");
        ImGui.textDisabled(connection.fromKey() + " -> " + connection.toKey());
        ImGui.spacing();

        String[] modes = {"flow", "each input"};
        if (ImGui.combo("Mode", selectedMode, modes)) {
            replaceSelected(connection.withMode(selectedMode.get() == 1
                ? EbslGraphConnectionMode.EACH_INPUT
                : EbslGraphConnectionMode.FLOW));
            save.run();
            statusSink.accept("updated link mode");
        }
        ImGui.setNextItemWidth(width);
        if (ImGui.inputText("Label", selectedLabel)) {
            replaceSelected(connection.withLabel(selectedLabel.get()));
            save.run();
            statusSink.accept("updated link label");
        }
        ImGui.textWrapped(connection.mode() == EbslGraphConnectionMode.EACH_INPUT
            ? "Runs the target once for this incoming link when the flow is materialized."
            : "Orders the target after all required incoming flow links.");
        if (ImGui.button("Delete link", 96.0f, 24.0f)) {
            connections.removeIf(candidate -> candidate.id().equals(selectedConnectionId));
            clearSelection();
            save.run();
            statusSink.accept("deleted link");
        }
    }

    void shiftAfterInsert(int lineNumber, ToIntFunction<String> lineReader, IntFunction<String> keyFactory) {
        shift(key -> {
            int existingLine = lineReader.applyAsInt(key);
            return existingLine > lineNumber ? keyFactory.apply(existingLine + 1) : key;
        });
    }

    void shiftAfterDelete(int lineNumber, ToIntFunction<String> lineReader, IntFunction<String> keyFactory) {
        shift(key -> {
            int existingLine = lineReader.applyAsInt(key);
            return existingLine > lineNumber ? keyFactory.apply(existingLine - 1) : key;
        });
    }

    private void addConnection(String fromKey, String toKey, Runnable save, Consumer<String> statusSink) {
        EbslGraphConnection connection = new EbslGraphConnection(fromKey, toKey);
        for (EbslGraphConnection existing : connections) {
            if (existing.fromKey().equals(fromKey) && existing.toKey().equals(toKey)) {
                select(existing);
                statusSink.accept("selected link");
                return;
            }
        }
        connections.add(connection);
        select(connection);
        save.run();
        statusSink.accept("connected nodes");
    }

    private void shift(UnaryOperator<String> mapper) {
        List<EbslGraphConnection> shifted = new ArrayList<>();
        for (EbslGraphConnection connection : connections) {
            EbslGraphConnection mapped = connection.remap(mapper);
            if (!mapped.fromKey().equals(mapped.toKey()) && !shifted.contains(mapped)) {
                shifted.add(mapped);
            }
        }
        connections.clear();
        connections.addAll(shifted);
        clearSelection();
    }

    private boolean handleEdgeHit(EbslGraphConnection connection, ScriptGraphNodeLayout from,
                                  ScriptGraphNodeLayout to, float graphZoom) {
        float portInset = 10.0f * graphZoom;
        float midX = (from.right() - portInset + to.x() + portInset) * 0.5f;
        float midY = (from.centerY() + to.centerY()) * 0.5f;
        float size = Math.max(18.0f, 18.0f * graphZoom);
        ImGui.setCursorScreenPos(midX - size * 0.5f, midY - size * 0.5f);
        ImGui.invisibleButton("##ebsl-graph-edge-" + connection.id(), size, size);
        if (!ImGui.isItemClicked()) {
            return false;
        }
        select(connection);
        return true;
    }

    private void selectFirstIncoming(String key) {
        for (EbslGraphConnection connection : connections) {
            if (connection.toKey().equals(key)) {
                select(connection);
                return;
            }
        }
    }

    private EbslGraphConnection selectedConnection() {
        for (EbslGraphConnection connection : connections) {
            if (connection.id().equals(selectedConnectionId)) {
                return connection;
            }
        }
        return null;
    }

    private void select(EbslGraphConnection connection) {
        selectedConnectionId = connection.id();
        selectedLabel.set(connection.label());
        selectedMode.set(connection.mode() == EbslGraphConnectionMode.EACH_INPUT ? 1 : 0);
    }

    private void replaceSelected(EbslGraphConnection replacement) {
        for (int i = 0; i < connections.size(); i++) {
            if (connections.get(i).id().equals(selectedConnectionId)) {
                connections.set(i, replacement);
                select(replacement);
                return;
            }
        }
    }
}
