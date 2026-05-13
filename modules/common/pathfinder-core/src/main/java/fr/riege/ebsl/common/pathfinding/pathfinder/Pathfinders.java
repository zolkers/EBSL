package fr.riege.ebsl.common.pathfinding.pathfinder;

import fr.riege.ebsl.common.pathfinding.pathing.InspectablePathfinder;
import fr.riege.ebsl.common.pathfinding.pathing.Pathfinder;
import fr.riege.ebsl.common.pathfinding.pathing.configuration.PathfinderConfiguration;

import java.util.Objects;

/**
 * Creates pathfinder instances behind their public contracts.
 *
 * <p>Consumers should depend on {@link Pathfinder} or {@link InspectablePathfinder} instead of
 * concrete search implementations. This keeps algorithm choices private to the pathfinder module
 * while leaving a small, stable construction surface for application code.</p>
 */
public final class Pathfinders {
    private Pathfinders() {
    }

    /**
     * Creates the default A* pathfinder exposed through the base pathfinder contract.
     *
     * @param configuration the search configuration to use
     * @return a pathfinder instance
     */
    public static Pathfinder aStar(PathfinderConfiguration configuration) {
        return new AStarPathfinder(Objects.requireNonNull(configuration, "configuration"));
    }

    /**
     * Creates the default A* pathfinder with diagnostic access.
     *
     * @param configuration the search configuration to use
     * @return a pathfinder instance with diagnostic counters
     */
    public static InspectablePathfinder inspectableAStar(PathfinderConfiguration configuration) {
        return new AStarPathfinder(Objects.requireNonNull(configuration, "configuration"));
    }
}
