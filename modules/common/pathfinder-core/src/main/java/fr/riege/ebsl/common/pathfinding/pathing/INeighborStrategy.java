package fr.riege.ebsl.common.pathfinding.pathing;

import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;
import fr.riege.ebsl.common.pathfinding.wrapper.PathVector;

/**
 * Supplies candidate offsets explored by the pathfinder.
 *
 * <p>Strategies can return global offsets or offsets tailored to the current position while keeping search expansion pluggable.</p>
 */
@FunctionalInterface
public interface INeighborStrategy {
    /**
     * Returns candidate neighbor offsets for the supplied search state.
 *
     * @return the requested values
     */
    Iterable<PathVector> getOffsets();

    /**
     * Returns candidate neighbor offsets for the supplied search state.
 *
     * @param currentPosition the current path position being expanded
     * @return the requested values
     */
    default Iterable<PathVector> getOffsets(PathPosition currentPosition) {
        return getOffsets();
    }
}
