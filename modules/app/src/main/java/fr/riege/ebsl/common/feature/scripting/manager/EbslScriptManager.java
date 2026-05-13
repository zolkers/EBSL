package fr.riege.ebsl.common.feature.scripting.manager;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.riege.ebsl.common.platform.layer.IStorageLayer;
import com.google.gson.JsonElement;
import com.google.gson.JsonArray;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
    private static final String JSON_LABEL = "label";
    private static final String JSON_POSITIONS = "positions";

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
        String normalized = fileName == null ? "" : fileName.trim().replace('\\', '/');
        int slash = normalized.lastIndexOf('/');
        if (slash >= 0) {
            normalized = normalized.substring(slash + 1);
        }
        normalized = normalized.toLowerCase(Locale.ROOT);
        if (normalized.isBlank()) {
            normalized = DEFAULT_FILE;
        }
        return normalized.endsWith(EXTENSION) ? normalized : normalized + EXTENSION;
    }

    public static String stripExtension(String fileName) {
        String normalized = normalizeFileName(fileName);
        return normalized.endsWith(EXTENSION)
            ? normalized.substring(0, normalized.length() - EXTENSION.length())
            : normalized;
    }

    public static String path(String fileName) {
        return DIRECTORY + "/" + normalizeFileName(fileName);
    }

    public Map<String, EbslGraphNodePosition> loadGraphLayout(String fileName) {
        return loadGraphDocument(fileName).positions();
    }

    public void saveGraphLayout(String fileName, Map<String, EbslGraphNodePosition> positions) {
        saveGraphDocument(fileName, new EbslGraphDocument(positions, List.of()));
    }

    public EbslGraphDocument loadGraphDocument(String fileName) {
        return storage.loadText(graphLayoutPath(fileName)).map(this::parseGraphDocument).orElseGet(EbslGraphDocument::empty);
    }

    public void saveGraphDocument(String fileName, EbslGraphDocument document) {
        JsonObject root = new JsonObject();
        JsonObject positions = new JsonObject();
        for (Map.Entry<String, EbslGraphNodePosition> entry : document.positions().entrySet()) {
            JsonObject node = new JsonObject();
            node.addProperty("x", entry.getValue().x());
            node.addProperty("y", entry.getValue().y());
            positions.add(entry.getKey(), node);
        }
        root.add(JSON_POSITIONS, positions);
        JsonArray connections = new JsonArray();
        for (EbslGraphConnection connection : document.connections()) {
            JsonObject edge = new JsonObject();
            edge.addProperty("id", connection.id());
            edge.addProperty("from", connection.fromKey());
            edge.addProperty("to", connection.toKey());
            edge.addProperty("mode", connection.mode().id());
            if (!connection.label().isBlank()) {
                edge.addProperty(JSON_LABEL, connection.label());
            }
            connections.add(edge);
        }
        root.add(JSON_CONNECTIONS, connections);
        storage.saveText(graphLayoutPath(fileName), root.toString());
    }

    private EbslGraphDocument parseGraphDocument(String json) {
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            JsonObject positionRoot = root.has(JSON_POSITIONS) && root.get(JSON_POSITIONS).isJsonObject()
                ? root.getAsJsonObject(JSON_POSITIONS)
                : root;
            return new EbslGraphDocument(parseGraphPositions(positionRoot), parseGraphConnections(root));
        } catch (RuntimeException exception) {
            return EbslGraphDocument.empty();
        }
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
        return new EbslGraphConnection(id, from, to, mode, label);
    }

    private static String graphLayoutPath(String fileName) {
        return GRAPH_LAYOUT_DIRECTORY + "/" + normalizeFileName(fileName) + ".json";
    }
}
