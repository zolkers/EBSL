package fr.riege.ebsl.common.task;

import fr.riege.ebsl.common.platform.EbslPlatform;
import fr.riege.ebsl.common.registry.MapRegistry;
import fr.riege.ebsl.common.settings.Setting;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class BotTaskRegistry {
    private static final MapRegistry<String, BotTask> TASKS = new MapRegistry<>(null);
    private static final Map<String, Boolean> lastEnabled = new HashMap<>();

    private BotTaskRegistry() {}

    public static void register(BotTask task) {
        TASKS.register(task.id(), task);
        lastEnabled.put(task.id(), task.isEnabled());
    }

    public static void update(EbslPlatform platform) {
        for (BotTask task : TASKS.values()) {
            syncLifecycle(task);
            if (task.isEnabled()) {
                task.tick(platform);
            }
        }
    }

    public static void render(EbslPlatform platform) {
        for (BotTask task : TASKS.values()) {
            if (task.isEnabled()) {
                task.render(platform);
            }
        }
    }

    public static void onSettingChanged(BotTask task, Setting<?> setting) {
        task.onSettingChanged(setting);
    }

    public static void saveSettings() {}

    public static void notifySettingChanged(BotTask task, Setting<?> setting) {
        onSettingChanged(task, setting);
    }

    public static void resetToDefaultsAndSave(BotTask task) {
        task.resetSettings();
    }

    public static void syncLifecycle(BotTask task) {
        boolean isEnabled = task.isEnabled();
        Boolean was = lastEnabled.put(task.id(), isEnabled);
        if (was != null && was != isEnabled && !isEnabled) {
            task.onDisable();
        }
    }

    public static Collection<BotTask> tasks() {
        return TASKS.values();
    }

    public static BotTask get(String id) {
        return TASKS.get(id);
    }
}
