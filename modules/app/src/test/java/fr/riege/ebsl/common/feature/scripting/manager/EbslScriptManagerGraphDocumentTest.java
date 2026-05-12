package fr.riege.ebsl.common.feature.scripting.manager;

import fr.riege.ebsl.common.platform.layer.IStorageLayer;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class EbslScriptManagerGraphDocumentTest {
    @Test
    void persistsEditableConnectionMetadata() {
        MemoryStorage storage = new MemoryStorage();
        EbslScriptManager manager = new EbslScriptManager(storage);
        EbslGraphDocument document = new EbslGraphDocument(
            Map.of("main.ebsl:1", new EbslGraphNodePosition(12.0f, 34.0f)),
            List.of(new EbslGraphConnection(
                "edge-1",
                "main.ebsl:1",
                "main.ebsl:2",
                EbslGraphConnectionMode.EACH_INPUT,
                "retry branch"
            ))
        );

        manager.saveGraphDocument("main.ebsl", document);
        EbslGraphDocument loaded = manager.loadGraphDocument("main.ebsl");

        assertEquals(document.positions(), loaded.positions());
        assertEquals(document.connections(), loaded.connections());
    }

    @Test
    void readsLegacyConnectionsAsFlowLinks() {
        MemoryStorage storage = new MemoryStorage();
        storage.saveText("scripts/.graph/main.ebsl.json", """
            {"connections":[{"from":"main.ebsl:1","to":"main.ebsl:2"}]}
            """);
        EbslGraphDocument loaded = new EbslScriptManager(storage).loadGraphDocument("main.ebsl");

        assertEquals(List.of(new EbslGraphConnection("main.ebsl:1", "main.ebsl:2")), loaded.connections());
    }

    private static final class MemoryStorage implements IStorageLayer {
        private final Map<String, String> text = new HashMap<>();

        @Override public void saveJson(String key, String json) {
            text.put(key, json);
        }

        @Override public Optional<String> loadJson(String key) {
            return Optional.ofNullable(text.get(key));
        }

        @Override public void saveText(String path, String text) {
            this.text.put(path, text);
        }

        @Override public Optional<String> loadText(String path) {
            return Optional.ofNullable(text.get(path));
        }
    }
}
