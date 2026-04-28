package fr.riege.ebsl.api.modules;

import fr.riege.ebsl.api.annotation.EbslApiOperation;
import fr.riege.ebsl.api.annotation.EbslApiSurface;
import fr.riege.ebsl.botting.module.PathfinderModule;
import fr.riege.ebsl.botting.registry.BotModuleRegistry;
import fr.riege.ebsl.botting.storage.BotModuleSettingsStore;
import fr.riege.ebsl.settings.Setting;

import java.util.Collection;

@EbslApiSurface(EbslApiSurface.Domain.MODULES)
public final class ModulesApi {
    public ModulesApi() {
    }

    @EbslApiOperation("Read all registered pathfinder modules.")
    public Collection<PathfinderModule> all() {
        return BotModuleRegistry.modules();
    }

    @EbslApiOperation("Find a pathfinder module by id.")
    public PathfinderModule get(String id) {
        return BotModuleRegistry.get(id);
    }

    @EbslApiOperation("Persist module settings.")
    public void saveSettings() {
        BotModuleSettingsStore.save();
    }

    @EbslApiOperation("Notify module lifecycle after a setting change.")
    public void notifySettingChanged(PathfinderModule module, Setting<?> setting) {
        BotModuleRegistry.onSettingChanged(module, setting);
    }

    @EbslApiOperation("Reset a module and notify lifecycle for every setting.")
    public void resetToDefaultsAndSave(PathfinderModule module) {
        module.resetSettings();
        saveSettings();
        for (Setting<?> setting : module.settings()) {
            notifySettingChanged(module, setting);
        }
    }
}
