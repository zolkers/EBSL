package fr.riege.ebsl.pathfinding.pathing;

import fr.riege.ebsl.pathfinding.wrapper.PathPosition;
import fr.riege.ebsl.pathfinding.wrapper.PathVector;

@FunctionalInterface
public interface INeighborStrategy {
    Iterable<PathVector> getOffsets();

    default Iterable<PathVector> getOffsets(PathPosition currentPosition) {
        return getOffsets();
    }
}
