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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.riege.ebsl.common.feature.scripting.highlight.EbslCodeEditorSettings;
import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;
import fr.riege.ebsl.common.platform.layer.IStorageLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;
import java.util.function.Function;

public final class CommonSettingsStore {
    private static final Logger LOGGER = LoggerFactory.getLogger("ebsl-settings");
    public static final String PATHFINDER_KEY = "pathfinder-settings";
    public static final String SCRIPT_EDITOR_KEY = "script-editor-settings";
    public static final String MODULES_KEY = "module-settings";
    public static final String TASKS_KEY = "task-settings";

    private CommonSettingsStore() {
    }

    public static void load(IStorageLayer storage) {
        try {
            storage.loadJson(PATHFINDER_KEY).ifPresent(json -> loadSettings(PathfinderSettings.all(), json));
            storage.loadJson(SCRIPT_EDITOR_KEY).ifPresent(json -> loadSettings(EbslCodeEditorSettings.all(), json));
        } catch (RuntimeException exception) {
            LOGGER.warn("Could not load pathfinder settings; keeping defaults.", exception);
        }
    }

    public static void loadBotSettings() {
        // Feature-owned module and task settings are loaded by the application adapter.
    }

    public static void save(IStorageLayer storage) {
        try {
            storage.saveJson(PATHFINDER_KEY, saveSettings(PathfinderSettings.all()).toString());
            storage.saveJson(SCRIPT_EDITOR_KEY, saveSettings(EbslCodeEditorSettings.all()).toString());
        } catch (RuntimeException exception) {
            LOGGER.warn("Could not save settings.", exception);
        }
    }

    public static void loadSettings(Iterable<Setting<?>> settings, String json) {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        for (Setting<?> setting : settings) {
            if (root.has(setting.id())) {
                try {
                    setting.load(root.get(setting.id()));
                } catch (RuntimeException exception) {
                    LOGGER.debug("Ignoring invalid setting value for '{}'.", setting.id(), exception);
                }
            }
        }
    }

    public static <T extends Settingable> void loadGroupedSettings(
        IStorageLayer storage,
        String key,
        Iterable<T> entries,
        Function<T, String> id,
        Consumer<T> afterLoad
    ) {
        loadGroupedSettings(storage, key, entries, id, Settingable::settings, afterLoad);
    }

    public static <T> void loadGroupedSettings(
        IStorageLayer storage,
        String key,
        Iterable<T> entries,
        Function<T, String> id,
        Function<T, Iterable<Setting<?>>> settings,
        Consumer<T> afterLoad
    ) {
        try {
            storage.loadJson(key).ifPresent(json -> loadGroupedSettings(json, entries, id, settings, afterLoad));
        } catch (RuntimeException exception) {
            LOGGER.warn("Could not load grouped settings '{}'; keeping defaults.", key, exception);
        }
    }

    public static <T extends Settingable> void saveGroupedSettings(
        IStorageLayer storage,
        String key,
        Iterable<T> entries,
        Function<T, String> id
    ) {
        saveGroupedSettings(storage, key, entries, id, Settingable::settings);
    }

    public static <T> void saveGroupedSettings(
        IStorageLayer storage,
        String key,
        Iterable<T> entries,
        Function<T, String> id,
        Function<T, Iterable<Setting<?>>> settings
    ) {
        try {
            storage.saveJson(key, saveGroupedSettings(entries, id, settings).toString());
        } catch (RuntimeException exception) {
            LOGGER.warn("Could not save grouped settings '{}'.", key, exception);
        }
    }

    private static <T> void loadGroupedSettings(
        String json,
        Iterable<T> entries,
        Function<T, String> id,
        Function<T, Iterable<Setting<?>>> settings,
        Consumer<T> afterLoad
    ) {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        for (T entry : entries) {
            String entryId = id.apply(entry);
            if (root.has(entryId) && root.get(entryId).isJsonObject()) {
                loadSettings(settings.apply(entry), root.getAsJsonObject(entryId).toString());
                afterLoad.accept(entry);
            }
        }
    }

    public static JsonObject saveSettings(Iterable<Setting<?>> settings) {
        JsonObject root = new JsonObject();
        for (Setting<?> setting : settings) {
            root.add(setting.id(), setting.toJson());
        }
        return root;
    }

    private static <T> JsonObject saveGroupedSettings(
        Iterable<T> entries,
        Function<T, String> id,
        Function<T, Iterable<Setting<?>>> settings
    ) {
        JsonObject root = new JsonObject();
        for (T entry : entries) {
            root.add(id.apply(entry), saveSettings(settings.apply(entry)));
        }
        return root;
    }
}
