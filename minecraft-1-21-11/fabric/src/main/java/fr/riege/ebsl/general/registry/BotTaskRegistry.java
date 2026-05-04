package fr.riege.ebsl.general.registry;

import fr.riege.ebsl.general.storage.BotTaskSettingsStore;
import fr.riege.ebsl.general.task.BotTask;
import fr.riege.ebsl.general.task.SpaceMobTask;
import fr.riege.ebsl.registry.MapRegistry;
import fr.riege.ebsl.settings.Setting;
import net.minecraft.client.Minecraft;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class BotTaskRegistry {
    private static final MapRegistry<String, BotTask> TASKS = new MapRegistry<>(null);
    private static final Map<String, Boolean> lastEnabled = new HashMap<>();
    private static boolean bootstrapped;

    private BotTaskRegistry() {
    }

    public static void bootstrap() {
        if (bootstrapped) return;
        bootstrapped = true;
        register(SpaceMobTask.INSTANCE);
        BotTaskSettingsStore.load();
        for (BotTask task : TASKS.values()) {
            lastEnabled.put(task.id(), task.isEnabled());
        }
    }

    public static void update(Minecraft mc) {
        for (BotTask task : TASKS.values()) {
            syncLifecycle(task);
            if (task.isEnabled()) {
                task.tick(mc);
            }
        }
    }

    public static void render(Minecraft mc) {
        for (BotTask task : TASKS.values()) {
            syncLifecycle(task);
            if (task.isEnabled()) {
                task.render(mc);
            }
        }
    }

    public static void register(BotTask task) {
        TASKS.register(task.id(), task);
    }

    public static void onSettingChanged(BotTask task, Setting<?> setting) {
        task.onSettingChanged(setting);
        syncLifecycle(task);
    }

    public static void syncLifecycle(BotTask task) {
        boolean isEnabled = task.isEnabled();
        Boolean was = lastEnabled.put(task.id(), isEnabled);
        if (was == null || was == isEnabled) return;
        if (!isEnabled) {
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
