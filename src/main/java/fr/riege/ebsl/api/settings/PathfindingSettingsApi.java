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
