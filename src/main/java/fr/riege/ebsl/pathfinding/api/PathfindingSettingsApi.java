package fr.riege.ebsl.pathfinding.api;

import fr.riege.ebsl.api.EbslApi;
import fr.riege.ebsl.settings.Setting;

import java.util.List;

public final class PathfindingSettingsApi {
    private PathfindingSettingsApi() {
    }

    public static List<Setting<?>> generalSettings() {
        return EbslApi.settings().pathfinding().general();
    }

    public static List<Setting<?>> movementCostSettings() {
        return EbslApi.settings().pathfinding().movementCosts();
    }

    public static List<Setting<?>> corridorCostSettings() {
        return EbslApi.settings().pathfinding().corridorCosts();
    }

    public static void save() {
        EbslApi.settings().pathfinding().save();
    }

    public static void resetToDefaultsAndSave() {
        EbslApi.settings().pathfinding().resetToDefaultsAndSave();
    }
}
