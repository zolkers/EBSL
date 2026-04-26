package fr.riege.ebsl.botting.registry;

import fr.riege.ebsl.botting.module.BlockTargetModule;
import fr.riege.ebsl.botting.module.KeyDisplayModule;
import fr.riege.ebsl.botting.module.MoveTypeOverlayModule;
import fr.riege.ebsl.botting.module.PathfinderBlockBlacklistModule;
import fr.riege.ebsl.botting.module.PathfinderModule;
import fr.riege.ebsl.botting.storage.BotModuleSettingsStore;
import fr.riege.ebsl.event.EventBus;
import fr.riege.ebsl.settings.Setting;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class BotModuleRegistry {
    private static final Map<String, PathfinderModule> MODULES = new LinkedHashMap<>();
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
        PathfinderModule previous = MODULES.putIfAbsent(module.id(), module);
        if (previous != null) {
            throw new IllegalStateException("Duplicate pathfinder module: " + module.id());
        }
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
