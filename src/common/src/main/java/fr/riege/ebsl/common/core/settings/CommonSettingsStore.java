package fr.riege.ebsl.common.core.settings;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.riege.ebsl.common.platform.layer.IStorageLayer;
import fr.riege.ebsl.common.feature.module.BotModuleRegistry;
import fr.riege.ebsl.common.feature.module.PathfinderModule;
import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;
import fr.riege.ebsl.common.feature.task.BotTask;
import fr.riege.ebsl.common.feature.task.BotTaskRegistry;

public final class CommonSettingsStore {
    private static final String PATHFINDER_KEY = "pathfinder-settings";
    private static final String MODULES_KEY = "module-settings";
    private static final String TASKS_KEY = "task-settings";

    private CommonSettingsStore() {
    }

    public static void load(IStorageLayer storage) {
        try {
            storage.load(PATHFINDER_KEY).ifPresent(json -> loadSettings(PathfinderSettings.all(), json));
        } catch (RuntimeException ignored) {
            // Keep defaults when the persisted config is unreadable or from an incompatible version.
        }
    }

    public static void loadBotSettings(IStorageLayer storage) {
        try {
            storage.load(MODULES_KEY).ifPresent(CommonSettingsStore::loadModuleSettings);
            storage.load(TASKS_KEY).ifPresent(CommonSettingsStore::loadTaskSettings);
        } catch (RuntimeException ignored) {
            // Keep defaults when persisted module/task config is incompatible.
        }
    }

    public static void save(IStorageLayer storage) {
        try {
            storage.save(PATHFINDER_KEY, saveSettings(PathfinderSettings.all()).toString());
            storage.save(MODULES_KEY, saveModuleSettings().toString());
            storage.save(TASKS_KEY, saveTaskSettings().toString());
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

    private static void loadModuleSettings(String json) {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        for (PathfinderModule module : BotModuleRegistry.modules()) {
            if (root.has(module.id()) && root.get(module.id()).isJsonObject()) {
                loadSettings(module.settings(), root.getAsJsonObject(module.id()).toString());
                BotModuleRegistry.syncLifecycle(module);
            }
        }
    }

    private static void loadTaskSettings(String json) {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        for (BotTask task : BotTaskRegistry.tasks()) {
            if (root.has(task.id()) && root.get(task.id()).isJsonObject()) {
                loadSettings(task.settings(), root.getAsJsonObject(task.id()).toString());
                BotTaskRegistry.syncLifecycle(task);
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

    private static JsonObject saveModuleSettings() {
        JsonObject root = new JsonObject();
        for (PathfinderModule module : BotModuleRegistry.modules()) {
            root.add(module.id(), saveSettings(module.settings()));
        }
        return root;
    }

    private static JsonObject saveTaskSettings() {
        JsonObject root = new JsonObject();
        for (BotTask task : BotTaskRegistry.tasks()) {
            root.add(task.id(), saveSettings(task.settings()));
        }
        return root;
    }
}
