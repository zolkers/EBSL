package fr.riege.ebsl.common.pathfinding.pathing.result;

import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;
import java.util.Collection;

/**
 * Defines the contract for {@code Path} implementations.
 */
public interface Path extends Iterable<PathPosition> {
    int length();
    PathPosition getStart();
    PathPosition getEnd();
    Collection<PathPosition> collect();
}
