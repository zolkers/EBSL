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

    @Test
    void mirrorsGraphConnectionsIntoScriptDirectives() {
        MemoryStorage storage = new MemoryStorage();
        storage.saveText("scripts/main.ebsl", """
            message first
            message target
            """);
        EbslScriptManager manager = new EbslScriptManager(storage);

        manager.saveGraphDocument("main.ebsl", new EbslGraphDocument(
            Map.of(),
            List.of(new EbslGraphConnection(
                "main.ebsl:1",
                "main.ebsl:2",
                EbslGraphConnectionMode.EACH_INPUT,
                "retry branch"
            ))
        ));

        assertEquals("""
            message first
            message target

            # Graph links
            # @link 1 -> 2 mode=each_input label="retry branch"
            """.stripTrailing(), storage.loadText("scripts/main.ebsl").orElseThrow());
    }

    @Test
    void readsScriptDirectivesAsGraphConnections() {
        MemoryStorage storage = new MemoryStorage();
        storage.saveText("scripts/main.ebsl", """
            message first
            message target

            # Graph links
            # @link 1 -> 2 mode=each_input label="retry branch"
            """);

        EbslGraphDocument loaded = new EbslScriptManager(storage).loadGraphDocument("main.ebsl");

        assertEquals(List.of(new EbslGraphConnection(
            "main.ebsl:1",
            "main.ebsl:2",
            EbslGraphConnectionMode.EACH_INPUT,
            "retry branch"
        )), loaded.connections());
    }

    @Test
    void executableSourceUsesScriptLinkDirectives() {
        MemoryStorage storage = new MemoryStorage();
        storage.saveText("scripts/main.ebsl", """
            message second
            message first

            # Graph links
            # @link 2 -> 1 mode=flow
            """);

        assertEquals("""
            message first
            message second
            """, new EbslScriptManager(storage).executableSource("main.ebsl"));
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
