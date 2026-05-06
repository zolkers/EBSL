package fr.riege.ebsl.common.module;

import fr.riege.ebsl.common.layer.IEventBus;
import fr.riege.ebsl.common.registry.MapRegistry;
import fr.riege.ebsl.common.settings.Setting;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class BotModuleRegistry {
    private static final MapRegistry<String, PathfinderModule> MODULES = new MapRegistry<>(null);
    private static final Map<String, Boolean> lastEnabled = new HashMap<>();
    private static IEventBus bus;

    private BotModuleRegistry() {}

    public static void bootstrap(IEventBus eventBus) {
        bus = eventBus;
        for (PathfinderModule module : MODULES.values()) {
            lastEnabled.put(module.id(), module.isEnabled());
            if (module.isEnabled()) {
                module.onEnable(bus);
            }
        }
    }

    public static void register(PathfinderModule module) {
        MODULES.register(module.id(), module);
    }

    public static void onSettingChanged(PathfinderModule module, Setting<?> setting) {
        module.onSettingChanged(setting);
        syncLifecycle(module);
    }

    public static void saveSettings() {}

    public static void notifySettingChanged(PathfinderModule module, Setting<?> setting) {
        onSettingChanged(module, setting);
    }

    public static void syncLifecycle(PathfinderModule module) {
        boolean isEnabled = module.isEnabled();
        Boolean was = lastEnabled.put(module.id(), isEnabled);
        if (was == null || was == isEnabled) return;
        if (isEnabled) {
            module.onEnable(bus);
        } else {
            module.onDisable();
        }
    }

    public static void resetToDefaultsAndSave(PathfinderModule module) {
        module.resetSettings();
    }

    public static Collection<PathfinderModule> modules() {
        return MODULES.values();
    }

    public static PathfinderModule get(String id) {
        return MODULES.get(id);
    }
}
