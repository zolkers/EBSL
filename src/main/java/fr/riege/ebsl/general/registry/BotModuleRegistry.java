package fr.riege.ebsl.general.registry;

import fr.riege.ebsl.general.module.overlay.BlockTargetModule;
import fr.riege.ebsl.general.module.overlay.KeyDisplayModule;
import fr.riege.ebsl.general.module.overlay.MoveTypeOverlayModule;
import fr.riege.ebsl.general.module.blacklist.PathfinderBlockBlacklistModule;
import fr.riege.ebsl.general.module.PathfinderModule;
import fr.riege.ebsl.general.storage.BotModuleSettingsStore;
import fr.riege.ebsl.event.EventBus;
import fr.riege.ebsl.registry.MapRegistry;
import fr.riege.ebsl.settings.Setting;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class BotModuleRegistry {
    private static final MapRegistry<String, PathfinderModule> MODULES = new MapRegistry<>(null);
    private static final Map<String, Boolean> lastEnabled = new HashMap<>();
    private static EventBus bus;
    private static boolean bootstrapped;

    private BotModuleRegistry() {}

    public static void bootstrap(EventBus eventBus) {
        if (bootstrapped) return;
        bootstrapped = true;
        bus = eventBus;
        register(PathfinderBlockBlacklistModule.INSTANCE);
        register(KeyDisplayModule.INSTANCE);
        register(BlockTargetModule.INSTANCE);
        register(MoveTypeOverlayModule.INSTANCE);
        BotModuleSettingsStore.load();
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

    public static Collection<PathfinderModule> modules() {
        return MODULES.values();
    }

    public static PathfinderModule get(String id) {
        return MODULES.get(id);
    }
}
