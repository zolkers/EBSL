package fr.riege.ebsl.general.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.riege.ebsl.EbslMod;
import fr.riege.ebsl.general.module.PathfinderModule;
import fr.riege.ebsl.general.registry.BotModuleRegistry;
import fr.riege.ebsl.settings.Setting;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class BotModuleSettingsStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("ebsl").resolve("modules.json");

    private BotModuleSettingsStore() {
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
            for (PathfinderModule module : BotModuleRegistry.modules()) {
                JsonObject moduleJson = root.has(module.id()) && root.get(module.id()).isJsonObject()
                    ? root.getAsJsonObject(module.id())
                    : null;
                if (moduleJson == null) {
                    continue;
                }
                for (Setting<?> setting : module.settings()) {
                    JsonElement element = moduleJson.get(setting.id());
                    if (element != null) {
                        setting.load(element);
                    }
                }
            }
        } catch (Exception exception) {
            EbslMod.LOGGER.warn("Failed to load EBSL module settings.", exception);
        }
    }

    public static void save() {
        JsonObject root = new JsonObject();
        for (PathfinderModule module : BotModuleRegistry.modules()) {
            JsonObject moduleJson = new JsonObject();
            for (Setting<?> setting : module.settings()) {
                moduleJson.add(setting.id(), setting.toJson());
            }
            root.add(module.id(), moduleJson);
        }
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(root));
        } catch (IOException exception) {
            EbslMod.LOGGER.warn("Failed to save EBSL module settings.", exception);
        }
    }
}
