package fr.riege.ebsl.common.feature.scripting.manager;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.riege.ebsl.common.platform.layer.IStorageLayer;
import com.google.gson.JsonElement;
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
        return storage.loadText(graphLayoutPath(fileName)).map(this::parseGraphLayout).orElseGet(Map::of);
    }

    public void saveGraphLayout(String fileName, Map<String, EbslGraphNodePosition> positions) {
        JsonObject root = new JsonObject();
        for (Map.Entry<String, EbslGraphNodePosition> entry : positions.entrySet()) {
            JsonObject node = new JsonObject();
            node.addProperty("x", entry.getValue().x());
            node.addProperty("y", entry.getValue().y());
            root.add(entry.getKey(), node);
        }
        storage.saveText(graphLayoutPath(fileName), root.toString());
    }

    private Map<String, EbslGraphNodePosition> parseGraphLayout(String json) {
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
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

    private static String graphLayoutPath(String fileName) {
        return GRAPH_LAYOUT_DIRECTORY + "/" + normalizeFileName(fileName) + ".json";
    }
}
