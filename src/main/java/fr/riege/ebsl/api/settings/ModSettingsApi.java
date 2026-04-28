package fr.riege.ebsl.api.settings;

import fr.riege.ebsl.api.annotation.EbslApiOperation;
import fr.riege.ebsl.api.annotation.EbslApiSurface;

@EbslApiSurface(EbslApiSurface.Domain.SETTINGS)
public final class ModSettingsApi {
    private final PathfindingSettingsApi pathfinding = new PathfindingSettingsApi();

    public ModSettingsApi() {
    }

    @EbslApiOperation("Access pathfinding settings.")
    public PathfindingSettingsApi pathfinding() {
        return pathfinding;
    }
}
