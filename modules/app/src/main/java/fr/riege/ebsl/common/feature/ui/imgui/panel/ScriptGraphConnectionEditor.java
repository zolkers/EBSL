package fr.riege.ebsl.common.feature.ui.imgui.panel;

import fr.riege.ebsl.common.feature.scripting.manager.EbslGraphConnection;
import imgui.ImDrawList;
import imgui.ImGui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

final class ScriptGraphConnectionEditor {
    private final List<EbslGraphConnection> connections = new ArrayList<>();
    private String connectingFromKey = "";
    private String hoveredInputKey = "";

    List<EbslGraphConnection> connections() {
        return List.copyOf(connections);
    }

    void load(List<EbslGraphConnection> connections) {
        this.connections.clear();
        this.connections.addAll(connections);
        connectingFromKey = "";
        hoveredInputKey = "";
    }

    void drawEdges(ImDrawList dl, List<ScriptGraphNodeLayout> layouts,
                   Map<String, ScriptGraphNodeLayout> layoutByKey, float graphZoom) {
        connections.removeIf(connection -> !layoutByKey.containsKey(connection.fromKey()) || !layoutByKey.containsKey(connection.toKey()));
        for (EbslGraphConnection connection : connections) {
            ScriptGraphNodeLayout from = layoutByKey.get(connection.fromKey());
            ScriptGraphNodeLayout to = layoutByKey.get(connection.toKey());
            if (from != null && to != null) {
                GraphEdgePainter.draw(dl, from, to, layouts, graphZoom);
            }
        }
        hoveredInputKey = "";
    }

    void handlePorts(ScriptGraphNodeLayout layout, float graphZoom, Runnable save, Consumer<String> statusSink) {
        float hit = Math.max(14.0f, 16.0f * graphZoom);
        float inputX = layout.x() + 10.0f * graphZoom;
        float outputX = layout.right() - 10.0f * graphZoom;
        float portY = layout.centerY();

        ImGui.setCursorScreenPos(inputX - hit * 0.5f, portY - hit * 0.5f);
        if (ImGui.invisibleButton("##ebsl-graph-input-" + layout.index(), hit, hit)) {
            detachIncoming(layout.node().key(), save, statusSink);
        }
        if (ImGui.isItemHovered()) {
            hoveredInputKey = layout.node().key();
        }

        ImGui.setCursorScreenPos(outputX - hit * 0.5f, portY - hit * 0.5f);
        ImGui.invisibleButton("##ebsl-graph-output-" + layout.index(), hit, hit);
        if (ImGui.isItemClicked()) {
            connectingFromKey = layout.node().key();
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
            save.run();
            statusSink.accept("detached node");
        }
    }

    void removeNode(String key) {
        connections.removeIf(connection -> connection.touches(key));
    }

    void shiftAfterInsert(int lineNumber, Function<String, Integer> lineReader, Function<Integer, String> keyFactory) {
        shift(key -> {
            int existingLine = lineReader.apply(key);
            return existingLine > lineNumber ? keyFactory.apply(existingLine + 1) : key;
        });
    }

    void shiftAfterDelete(int lineNumber, Function<String, Integer> lineReader, Function<Integer, String> keyFactory) {
        shift(key -> {
            int existingLine = lineReader.apply(key);
            return existingLine > lineNumber ? keyFactory.apply(existingLine - 1) : key;
        });
    }

    private void addConnection(String fromKey, String toKey, Runnable save, Consumer<String> statusSink) {
        EbslGraphConnection connection = new EbslGraphConnection(fromKey, toKey);
        if (!connections.contains(connection)) {
            connections.add(connection);
            save.run();
            statusSink.accept("connected nodes");
        }
    }

    private void detachIncoming(String key, Runnable save, Consumer<String> statusSink) {
        if (connections.removeIf(connection -> connection.toKey().equals(key))) {
            save.run();
            statusSink.accept("detached input");
        }
    }

    private void shift(Function<String, String> mapper) {
        List<EbslGraphConnection> shifted = new ArrayList<>();
        for (EbslGraphConnection connection : connections) {
            EbslGraphConnection mapped = new EbslGraphConnection(mapper.apply(connection.fromKey()), mapper.apply(connection.toKey()));
            if (!mapped.fromKey().equals(mapped.toKey()) && !shifted.contains(mapped)) {
                shifted.add(mapped);
            }
        }
        connections.clear();
        connections.addAll(shifted);
    }
}
