package fr.riege.ebsl.common.pathfinding.pathing;

import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;
import fr.riege.ebsl.common.pathfinding.wrapper.PathVector;

/**
 * Defines the contract for {@code INeighborStrategy} implementations.
 */
@FunctionalInterface
public interface INeighborStrategy {
    Iterable<PathVector> getOffsets();

    default Iterable<PathVector> getOffsets(PathPosition currentPosition) {
        return getOffsets();
    }
}
