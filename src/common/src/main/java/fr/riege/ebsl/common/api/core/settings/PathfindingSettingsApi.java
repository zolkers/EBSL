package fr.riege.ebsl.common.api.core.settings;

import fr.riege.ebsl.common.api.core.annotation.EbslApiOperation;
import fr.riege.ebsl.common.api.core.annotation.EbslApiSurface;
import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;
import fr.riege.ebsl.common.platform.service.EbslServices;
import fr.riege.ebsl.common.core.settings.CommonSettingsStore;
import fr.riege.ebsl.common.core.settings.Setting;

import java.util.List;

@EbslApiSurface(EbslApiSurface.Domain.SETTINGS)
public final class PathfindingSettingsApi {
    PathfindingSettingsApi() {
    }

    @EbslApiOperation("Read every pathfinding setting.")
    public List<Setting<?>> all() {
        return PathfinderSettings.all();
    }

    @EbslApiOperation("Read general pathfinding settings.")
    public List<Setting<?>> general() {
        return PathfinderSettings.generalSettings();
    }

    @EbslApiOperation("Read movement cost settings.")
    public List<Setting<?>> movementCosts() {
        return PathfinderSettings.movementCostSettings();
    }

    @EbslApiOperation("Read corridor and centering cost settings.")
    public List<Setting<?>> corridorCosts() {
        return PathfinderSettings.corridorCostSettings();
    }

    @EbslApiOperation("Read steering settings.")
    public List<Setting<?>> steering() {
        return PathfinderSettings.steeringSettings();
    }

    @EbslApiOperation("Read planning limit settings.")
    public List<Setting<?>> planningLimits() {
        return PathfinderSettings.planningLimitSettings();
    }

    @EbslApiOperation("Read execution settings.")
    public List<Setting<?>> execution() {
        return PathfinderSettings.executionSettings();
    }

    @EbslApiOperation("Read recovery settings.")
    public List<Setting<?>> recovery() {
        return PathfinderSettings.recoverySettings();
    }

    @EbslApiOperation("Read long-range navigation settings.")
    public List<Setting<?>> longRange() {
        return PathfinderSettings.longRangeSettings();
    }

    @EbslApiOperation("Read rotation settings.")
    public List<Setting<?>> rotation() {
        return PathfinderSettings.rotationSettings();
    }

    @EbslApiOperation("Read smoothing settings.")
    public List<Setting<?>> smoothing() {
        return PathfinderSettings.smoothingSettings();
    }

    @EbslApiOperation("Read path check settings.")
    public List<Setting<?>> pathChecks() {
        return PathfinderSettings.pathCheckSettings();
    }

    @EbslApiOperation("Persist all common settings through the installed storage layer.")
    public void save() {
        CommonSettingsStore.save(EbslServices.platform().storage());
    }

    @EbslApiOperation("Reset pathfinding settings and persist defaults.")
    public void resetToDefaultsAndSave() {
        PathfinderSettings.resetToDefaults();
        save();
    }
}
