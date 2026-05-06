package fr.riege.ebsl.common.settings;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.riege.ebsl.common.layer.IStorageLayer;
import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;

public final class CommonSettingsStore {
    private static final String PATHFINDER_KEY = "pathfinder-settings";

    private CommonSettingsStore() {
    }

    public static void load(IStorageLayer storage) {
        try {
            storage.load(PATHFINDER_KEY).ifPresent(json -> loadSettings(PathfinderSettings.all(), json));
        } catch (RuntimeException ignored) {
            // Keep defaults when the persisted config is unreadable or from an incompatible version.
        }
    }

    public static void save(IStorageLayer storage) {
        try {
            storage.save(PATHFINDER_KEY, saveSettings(PathfinderSettings.all()).toString());
        } catch (RuntimeException ignored) {
            // Settings persistence should not be able to crash the client tick.
        }
    }

    private static void loadSettings(Iterable<Setting<?>> settings, String json) {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        for (Setting<?> setting : settings) {
            if (root.has(setting.id())) {
                try {
                    setting.load(root.get(setting.id()));
                } catch (RuntimeException ignored) {
                    // Ignore invalid values one by one so the rest of the file still loads.
                }
            }
        }
    }

    private static JsonObject saveSettings(Iterable<Setting<?>> settings) {
        JsonObject root = new JsonObject();
        for (Setting<?> setting : settings) {
            root.add(setting.id(), setting.toJson());
        }
        return root;
    }
}
