package fr.riege.ebsl.common.api.feature.modules;

import fr.riege.ebsl.common.api.core.annotation.EbslApiOperation;
import fr.riege.ebsl.common.api.core.annotation.EbslApiSurface;
import fr.riege.ebsl.common.feature.module.BotModuleRegistry;
import fr.riege.ebsl.common.feature.module.PathfinderModule;
import fr.riege.ebsl.common.platform.service.EbslServices;
import fr.riege.ebsl.common.core.settings.CommonSettingsStore;
import fr.riege.ebsl.common.core.settings.Setting;

import java.util.Collection;

@EbslApiSurface(EbslApiSurface.Domain.MODULES)
public final class ModulesApi {
    @EbslApiOperation("Read all registered pathfinder modules.")
    public Collection<PathfinderModule> all() {
        return BotModuleRegistry.modules();
    }

    @EbslApiOperation("Find a pathfinder module by id.")
    public PathfinderModule get(String id) {
        return BotModuleRegistry.get(id);
    }

    @EbslApiOperation("Persist module settings through the installed storage layer.")
    public void saveSettings() {
        CommonSettingsStore.save(EbslServices.platform().storage());
    }

    @EbslApiOperation("Notify module lifecycle after a setting change.")
    public void notifySettingChanged(PathfinderModule module, Setting<?> setting) {
        BotModuleRegistry.onSettingChanged(module, setting);
    }

    @EbslApiOperation("Reset a module and persist settings.")
    public void resetToDefaultsAndSave(PathfinderModule module) {
        BotModuleRegistry.resetToDefaultsAndSave(module);
        saveSettings();
        for (Setting<?> setting : module.settings()) {
            notifySettingChanged(module, setting);
        }
    }
}
