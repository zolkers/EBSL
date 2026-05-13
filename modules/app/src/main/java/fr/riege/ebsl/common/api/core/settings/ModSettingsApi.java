package fr.riege.ebsl.common.api.core.settings;

import fr.riege.ebsl.common.api.core.annotation.EbslApiOperation;
import fr.riege.ebsl.common.api.core.annotation.EbslApiSurface;

@EbslApiSurface(EbslApiSurface.Domain.SETTINGS)
public final class ModSettingsApi {
    private final PathfindingSettingsApi pathfinding = new PathfindingSettingsApi();

    @EbslApiOperation("Access pathfinding settings.")
    public PathfindingSettingsApi pathfinding() {
        return pathfinding;
    }
}
