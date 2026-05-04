package fr.riege.ebsl.general.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.riege.ebsl.EbslMod;
import fr.riege.ebsl.general.registry.BotTaskRegistry;
import fr.riege.ebsl.general.task.BotTask;
import fr.riege.ebsl.settings.Setting;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class BotTaskSettingsStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("ebsl").resolve("tasks.json");

    private BotTaskSettingsStore() {
    }

    public static void load() {
        if (!Files.exists(FILE)) {
            return;
        }
        try {
            JsonObject root = GSON.fromJson(Files.readString(FILE), JsonObject.class);
            if (root == null) {
                return;
            }
            for (BotTask task : BotTaskRegistry.tasks()) {
                JsonObject taskJson = root.has(task.id()) && root.get(task.id()).isJsonObject()
                    ? root.getAsJsonObject(task.id())
                    : null;
                if (taskJson == null) {
                    continue;
                }
                for (Setting<?> setting : task.settings()) {
                    JsonElement element = taskJson.get(setting.id());
                    if (element != null) {
                        setting.load(element);
                    }
                }
            }
        } catch (Exception exception) {
            EbslMod.LOGGER.warn("Failed to load EBSL task settings.", exception);
        }
    }

    public static void save() {
        JsonObject root = new JsonObject();
        for (BotTask task : BotTaskRegistry.tasks()) {
            JsonObject taskJson = new JsonObject();
            for (Setting<?> setting : task.settings()) {
                taskJson.add(setting.id(), setting.toJson());
            }
            root.add(task.id(), taskJson);
        }
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(root));
        } catch (IOException exception) {
            EbslMod.LOGGER.warn("Failed to save EBSL task settings.", exception);
        }
    }
}
