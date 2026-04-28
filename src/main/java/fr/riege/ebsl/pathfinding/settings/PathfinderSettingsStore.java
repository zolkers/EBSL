package fr.riege.ebsl.pathfinding.settings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.riege.ebsl.EbslMod;
import fr.riege.ebsl.settings.Setting;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PathfinderSettingsStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("ebsl").resolve("pathfinder.json");

    private PathfinderSettingsStore() {
    }

    public static void load() {
        if (!Files.exists(FILE)) {
            return;
        }
        try {
            JsonObject root = GSON.fromJson(Files.readString(FILE), JsonObject.class);
            if (root != null) {
                for (Setting<?> setting : PathfinderSettings.instance().settings()) {
                    JsonElement element = root.get(setting.id());
                    if (element != null) {
                        setting.load(element);
                    }
                }
            }
        } catch (Exception exception) {
            EbslMod.LOGGER.warn("Failed to load EBSL pathfinder settings.", exception);
        }
    }

    public static void save() {
        JsonObject root = new JsonObject();
        for (Setting<?> setting : PathfinderSettings.instance().settings()) {
            root.add(setting.id(), setting.toJson());
        }
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(root));
        } catch (IOException exception) {
            EbslMod.LOGGER.warn("Failed to save EBSL pathfinder settings.", exception);
        }
    }
}
