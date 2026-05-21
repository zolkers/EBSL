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

package fr.riege.ebsl.common.feature.scripting.manager;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.riege.ebsl.common.platform.layer.IStorageLayer;

import java.util.*;

public final class EbslScriptManager {
    public static final String DIRECTORY = "scripts";
    private static final String GRAPH_LAYOUT_DIRECTORY = DIRECTORY + "/.graph";
    public static final String EXTENSION = ".ebsl";
    public static final String DEFAULT_FILE = "main.ebsl";
    public static final String DEFAULT_SOURCE = """
        # EBSL script
        start
        message "EBSL ready"
        """;
    private static final String JSON_CONNECTIONS = "connections";
    private static final String JSON_DIRECTION = "direction";
    private static final String JSON_FIELDS = "fields";
    private static final String JSON_LABEL = "label";
    private static final String JSON_MULTIPLE = "multiple";
    private static final String JSON_NODES = "nodes";
    private static final String JSON_POSITIONS = "positions";
    private static final String JSON_FROM_PORT = "fromPort";
    private static final String JSON_TO_PORT = "toPort";

    private final IStorageLayer storage;

    public EbslScriptManager(IStorageLayer storage) {
        this.storage = storage;
    }

    public List<String> scripts() {
        List<String> files = storage.listTextFiles(DIRECTORY, EXTENSION);
        if (files.isEmpty()) {
            return List.of(DEFAULT_FILE);
        }
        return files;
    }

    public EbslScriptDocument load(String fileName) {
        String normalized = normalizeFileName(fileName);
        String source = storage.loadText(path(normalized)).orElse(DEFAULT_SOURCE);
        return new EbslScriptDocument(normalized, source);
    }

    public String executableSource(String fileName) {
        String normalized = normalizeFileName(fileName);
        String source = storage.loadText(path(normalized)).orElse(DEFAULT_SOURCE);
        return EbslGraphExecutionPlanner.plan(normalized, source, loadGraphDocument(normalized));
    }

    public EbslScriptDocument create(String fileName) {
        String normalized = normalizeFileName(fileName);
        storage.saveText(path(normalized), DEFAULT_SOURCE);
        return new EbslScriptDocument(normalized, DEFAULT_SOURCE);
    }

    public void save(String fileName, String source) {
        storage.saveText(path(normalizeFileName(fileName)), source == null ? "" : source);
    }

    public void delete(String fileName) {
        String normalized = normalizeFileName(fileName);
        storage.deleteText(path(normalized));
        storage.deleteText(graphLayoutPath(normalized));
    }

    public static String normalizeFileName(String fileName) {
        return EbslScriptFileNames.normalize(fileName);
    }

    public static String stripExtension(String fileName) {
        return EbslScriptFileNames.stripExtension(fileName);
    }

    public static String path(String fileName) {
        return DIRECTORY + "/" + normalizeFileName(fileName);
    }

    public Map<String, EbslGraphNodePosition> loadGraphLayout(String fileName) {
        return loadGraphDocument(fileName).positions();
    }

    public void saveGraphLayout(String fileName, Map<String, EbslGraphNodePosition> positions) {
        saveGraphDocument(fileName, new EbslGraphDocument(positions, loadGraphDocument(fileName).connections()));
    }

    public EbslGraphDocument loadGraphDocument(String fileName) {
        String normalized = normalizeFileName(fileName);
        EbslGraphDocument document = storage.loadText(graphLayoutPath(normalized)).map(this::parseGraphDocument).orElseGet(EbslGraphDocument::empty);
        List<EbslGraphConnection> scriptConnections = storage.loadText(path(normalized))
            .map(source -> EbslGraphScriptLinks.parse(normalized, source))
            .orElse(List.of());
        return scriptConnections.isEmpty()
            ? document
            : new EbslGraphDocument(document.positions(), scriptConnections);
    }

    public void saveGraphDocument(String fileName, EbslGraphDocument document) {
        String normalized = normalizeFileName(fileName);
        JsonObject root = new JsonObject();
        JsonObject positions = new JsonObject();
        for (Map.Entry<String, EbslGraphNodePosition> entry : document.positions().entrySet()) {
            JsonObject node = new JsonObject();
            node.addProperty("x", entry.getValue().x());
            node.addProperty("y", entry.getValue().y());
            positions.add(entry.getKey(), node);
        }
        root.add(JSON_POSITIONS, positions);
        root.add(JSON_NODES, serializeGraphNodes(document.nodes()));
        JsonArray connections = new JsonArray();
        for (EbslGraphConnection connection : document.connections()) {
            JsonObject edge = new JsonObject();
            edge.addProperty("id", connection.id());
            edge.addProperty("from", connection.fromKey());
            edge.addProperty(JSON_FROM_PORT, connection.fromPort());
            edge.addProperty("to", connection.toKey());
            edge.addProperty(JSON_TO_PORT, connection.toPort());
            edge.addProperty("mode", connection.mode().id());
            if (!connection.label().isBlank()) {
                edge.addProperty(JSON_LABEL, connection.label());
            }
            connections.add(edge);
        }
        root.add(JSON_CONNECTIONS, connections);
        storage.saveText(graphLayoutPath(normalized), root.toString());
        syncScriptLinkDirectives(normalized, document.connections());
    }

    public static String syncGraphLinkDirectives(String fileName, String source, List<EbslGraphConnection> connections) {
        return EbslGraphScriptLinks.sync(fileName, source, connections);
    }

    private EbslGraphDocument parseGraphDocument(String json) {
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            JsonObject positionRoot = root.has(JSON_POSITIONS) && root.get(JSON_POSITIONS).isJsonObject()
                ? root.getAsJsonObject(JSON_POSITIONS)
                : root;
            return new EbslGraphDocument(parseGraphPositions(positionRoot), parseGraphConnections(root), parseGraphNodes(root));
        } catch (RuntimeException exception) {
            return EbslGraphDocument.empty();
        }
    }

    private JsonArray serializeGraphNodes(Map<String, EbslGraphNode> nodes) {
        JsonArray values = new JsonArray();
        for (EbslGraphNode node : nodes.values()) {
            JsonObject value = new JsonObject();
            value.addProperty("id", node.id());
            value.addProperty("type", node.type());
            value.add(JSON_FIELDS, serializeStringMap(node.fields()));
            value.add("inputs", serializeGraphPorts(node.inputs()));
            value.add("outputs", serializeGraphPorts(node.outputs()));
            values.add(value);
        }
        return values;
    }

    private JsonObject serializeStringMap(Map<String, String> values) {
        JsonObject root = new JsonObject();
        for (Map.Entry<String, String> entry : values.entrySet()) {
            root.addProperty(entry.getKey(), entry.getValue());
        }
        return root;
    }

    private JsonArray serializeGraphPorts(List<EbslGraphPort> ports) {
        JsonArray values = new JsonArray();
        for (EbslGraphPort port : ports) {
            JsonObject value = new JsonObject();
            value.addProperty("id", port.id());
            value.addProperty(JSON_LABEL, port.label());
            value.addProperty(JSON_DIRECTION, port.direction().id());
            value.addProperty("kind", port.kind().id());
            value.addProperty(JSON_MULTIPLE, port.multiple());
            values.add(value);
        }
        return values;
    }

    private Map<String, EbslGraphNodePosition> parseGraphPositions(JsonObject root) {
        try {
            Map<String, EbslGraphNodePosition> positions = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                if (!entry.getValue().isJsonObject()) {
                    continue;
                }
                JsonObject node = entry.getValue().getAsJsonObject();
                if (node.has("x") && node.has("y")) {
                    positions.put(entry.getKey(), new EbslGraphNodePosition(
                        node.get("x").getAsFloat(),
                        node.get("y").getAsFloat()
                    ));
                }
            }
            return positions;
        } catch (RuntimeException exception) {
            return Map.of();
        }
    }

    private List<EbslGraphConnection> parseGraphConnections(JsonObject root) {
        if (!root.has(JSON_CONNECTIONS) || !root.get(JSON_CONNECTIONS).isJsonArray()) {
            return List.of();
        }
        List<EbslGraphConnection> connections = new ArrayList<>();
        for (JsonElement element : root.getAsJsonArray(JSON_CONNECTIONS)) {
            EbslGraphConnection connection = parseGraphConnection(element);
            if (connection != null) {
                connections.add(connection);
            }
        }
        return connections;
    }

    private EbslGraphConnection parseGraphConnection(JsonElement element) {
        if (!element.isJsonObject()) {
            return null;
        }
        JsonObject edge = element.getAsJsonObject();
        if (!edge.has("from") || !edge.has("to")) {
            return null;
        }
        String from = edge.get("from").getAsString();
        String to = edge.get("to").getAsString();
        if (from.isBlank() || to.isBlank() || from.equals(to)) {
            return null;
        }
        String id = edge.has("id") ? edge.get("id").getAsString() : "";
        EbslGraphConnectionMode mode = edge.has("mode")
            ? EbslGraphConnectionMode.byId(edge.get("mode").getAsString())
            : EbslGraphConnectionMode.FLOW;
        String label = edge.has(JSON_LABEL) ? edge.get(JSON_LABEL).getAsString() : "";
        String fromPort = edge.has(JSON_FROM_PORT) ? edge.get(JSON_FROM_PORT).getAsString() : EbslGraphConnection.DEFAULT_FLOW_PORT;
        String toPort = edge.has(JSON_TO_PORT) ? edge.get(JSON_TO_PORT).getAsString() : EbslGraphConnection.DEFAULT_FLOW_PORT;
        return new EbslGraphConnection(id, from, fromPort, to, toPort, mode, label);
    }

    private Map<String, EbslGraphNode> parseGraphNodes(JsonObject root) {
        if (!root.has(JSON_NODES) || !root.get(JSON_NODES).isJsonArray()) {
            return Map.of();
        }
        Map<String, EbslGraphNode> nodes = new LinkedHashMap<>();
        for (JsonElement element : root.getAsJsonArray(JSON_NODES)) {
            EbslGraphNode node = parseGraphNode(element);
            if (node != null) {
                nodes.put(node.id(), node);
            }
        }
        return nodes;
    }

    private EbslGraphNode parseGraphNode(JsonElement element) {
        if (!element.isJsonObject()) {
            return null;
        }
        JsonObject node = element.getAsJsonObject();
        if (!node.has("id") || !node.has("type")) {
            return null;
        }
        return new EbslGraphNode(
            node.get("id").getAsString(),
            node.get("type").getAsString(),
            parseStringMap(node, JSON_FIELDS),
            parseGraphPorts(node, "inputs", EbslGraphPortDirection.INPUT),
            parseGraphPorts(node, "outputs", EbslGraphPortDirection.OUTPUT));
    }

    private Map<String, String> parseStringMap(JsonObject root, String name) {
        if (!root.has(name) || !root.get(name).isJsonObject()) {
            return Map.of();
        }
        Map<String, String> values = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : root.getAsJsonObject(name).entrySet()) {
            if (entry.getValue().isJsonPrimitive()) {
                values.put(entry.getKey(), entry.getValue().getAsString());
            }
        }
        return values;
    }

    private List<EbslGraphPort> parseGraphPorts(JsonObject root, String name, EbslGraphPortDirection direction) {
        if (!root.has(name) || !root.get(name).isJsonArray()) {
            return List.of();
        }
        List<EbslGraphPort> ports = new ArrayList<>();
        for (JsonElement element : root.getAsJsonArray(name)) {
            EbslGraphPort port = parseGraphPort(element, direction);
            if (port != null) {
                ports.add(port);
            }
        }
        return ports;
    }

    private EbslGraphPort parseGraphPort(JsonElement element, EbslGraphPortDirection fallbackDirection) {
        if (!element.isJsonObject()) {
            return null;
        }
        JsonObject port = element.getAsJsonObject();
        if (!port.has("id")) {
            return null;
        }
        EbslGraphPortDirection direction = port.has(JSON_DIRECTION)
            ? EbslGraphPortDirection.byId(port.get(JSON_DIRECTION).getAsString())
            : fallbackDirection;
        EbslGraphPortKind kind = port.has("kind")
            ? EbslGraphPortKind.byId(port.get("kind").getAsString())
            : EbslGraphPortKind.FLOW;
        String label = port.has(JSON_LABEL) ? port.get(JSON_LABEL).getAsString() : port.get("id").getAsString();
        boolean multiple = port.has(JSON_MULTIPLE) && port.get(JSON_MULTIPLE).getAsBoolean();
        return new EbslGraphPort(port.get("id").getAsString(), label, direction, kind, multiple);
    }

    private static String graphLayoutPath(String fileName) {
        return GRAPH_LAYOUT_DIRECTORY + "/" + normalizeFileName(fileName) + ".json";
    }

    private void syncScriptLinkDirectives(String fileName, List<EbslGraphConnection> connections) {
        String normalized = normalizeFileName(fileName);
        storage.loadText(path(normalized))
            .map(source -> EbslGraphScriptLinks.sync(normalized, source, connections))
            .ifPresent(source -> storage.saveText(path(normalized), source));
    }
}
