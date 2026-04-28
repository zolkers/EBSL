package fr.riege.ebsl.api.settings;

import fr.riege.ebsl.api.annotation.EbslApiOperation;
import fr.riege.ebsl.api.annotation.EbslApiSurface;
import fr.riege.ebsl.pathfinding.settings.PathfinderSettings;
import fr.riege.ebsl.pathfinding.settings.PathfinderSettingsStore;
import fr.riege.ebsl.settings.Setting;

import java.util.List;

@EbslApiSurface(EbslApiSurface.Domain.SETTINGS)
public final class PathfindingSettingsApi {
    PathfindingSettingsApi() {
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

    @EbslApiOperation("Read path execution steering settings.")
    public List<Setting<?>> steering() {
        return PathfinderSettings.steeringSettings();
    }

    @EbslApiOperation("Read path planning limit settings.")
    public List<Setting<?>> planningLimits() {
        return PathfinderSettings.planningLimitSettings();
    }

    @EbslApiOperation("Read path execution settings.")
    public List<Setting<?>> execution() {
        return PathfinderSettings.executionSettings();
    }

    @EbslApiOperation("Read path recovery settings.")
    public List<Setting<?>> recovery() {
        return PathfinderSettings.recoverySettings();
    }

    @EbslApiOperation("Read long-range pathing settings.")
    public List<Setting<?>> longRange() {
        return PathfinderSettings.longRangeSettings();
    }

    @EbslApiOperation("Read path rotation and camera settings.")
    public List<Setting<?>> rotation() {
        return PathfinderSettings.rotationSettings();
    }

    @EbslApiOperation("Read path smoothing and path post-processing settings.")
    public List<Setting<?>> smoothing() {
        return PathfinderSettings.smoothingSettings();
    }

    @EbslApiOperation("Read path health check settings.")
    public List<Setting<?>> pathChecks() {
        return PathfinderSettings.pathCheckSettings();
    }

    @EbslApiOperation("Apply and persist pathfinding settings.")
    public void save() {
        PathfinderSettings.apply();
        PathfinderSettingsStore.save();
    }

    @EbslApiOperation("Reset pathfinding settings and persist defaults.")
    public void resetToDefaultsAndSave() {
        PathfinderSettings.resetToDefaults();
        PathfinderSettingsStore.save();
    }
}
