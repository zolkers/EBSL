package fr.riege.ebsl.botting.registry;

import fr.riege.ebsl.botting.module.BotModule;
import fr.riege.ebsl.botting.module.KeyDisplayModule;
import fr.riege.ebsl.botting.module.PathfinderBlockBlacklistModule;
import fr.riege.ebsl.botting.storage.BotModuleSettingsStore;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public final class BotModuleRegistry {
    private static final Map<String, BotModule> MODULES = new LinkedHashMap<>();
    private static boolean bootstrapped;

    private BotModuleRegistry() {
    }

    public static void bootstrap() {
        if (bootstrapped) {
            return;
        }
        bootstrapped = true;
        register(PathfinderBlockBlacklistModule.INSTANCE);
        register(KeyDisplayModule.INSTANCE);
        BotModuleSettingsStore.load();
    }

    public static void register(BotModule module) {
        BotModule previous = MODULES.putIfAbsent(module.id(), module);
        if (previous != null) {
            throw new IllegalStateException("Duplicate bot module: " + module.id());
        }
    }

    public static Collection<BotModule> modules() {
        return MODULES.values();
    }

    public static BotModule get(String id) {
        return MODULES.get(id);
    }
}
