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

package fr.riege.ebsl.common.core.settings;

import fr.riege.ebsl.common.platform.layer.IStorageLayer;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommonSettingsStoreTest {
    @Test
    void loadsAndSavesGroupedSettings() {
        MemoryStorage storage = new MemoryStorage();
        TestEntry entry = new TestEntry("alpha");
        storage.saveJson("entries", "{\"alpha\":{\"amount\":7}}");

        CommonSettingsStore.loadGroupedSettings(
            storage,
            "entries",
            List.of(entry),
            TestEntry::id,
            TestEntry::onLoaded);
        CommonSettingsStore.saveGroupedSettings(storage, "entries", List.of(entry), TestEntry::id);

        assertEquals(7, entry.amount.value());
        assertEquals(1, entry.loadCallbacks);
        assertEquals("{\"alpha\":{\"amount\":7}}", storage.saved.get("entries"));
    }

    @Test
    void ignoresBotSettingsInSharedStore() {
        CommonSettingsStore.loadBotSettings();
    }

    private static final class TestEntry extends Settingable {
        private final String id;
        private final IntSetting amount = registerSetting(new IntSetting("amount", "Amount", 1, 0, 10));
        private int loadCallbacks;

        private TestEntry(String id) {
            this.id = id;
        }

        private String id() {
            return id;
        }

        private void onLoaded() {
            loadCallbacks++;
        }
    }

    private static final class MemoryStorage implements IStorageLayer {
        private final Map<String, String> saved = new HashMap<>();

        @Override
        public void saveJson(String key, String json) {
            saved.put(key, json);
        }

        @Override
        public Optional<String> loadJson(String key) {
            return Optional.ofNullable(saved.get(key));
        }

        @Override
        public void saveText(String path, String text) {
            saved.put(path, text);
        }

        @Override
        public Optional<String> loadText(String path) {
            return Optional.ofNullable(saved.get(path));
        }
    }
}
