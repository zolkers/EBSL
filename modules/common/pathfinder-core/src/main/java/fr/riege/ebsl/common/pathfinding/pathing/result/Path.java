package fr.riege.ebsl.common.pathfinding.pathing.result;

import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;
import java.util.Collection;

/**
 * Represents an ordered sequence of positions returned by the pathfinder.
 *
 * <p>Paths are iterable and expose start/end metadata while preserving collection access for rendering and execution code.</p>
 */
public interface Path extends Iterable<PathPosition> {
    /**
     * Returns the number of positions contained in the path.
 *
     * @return the value defined by this contract
     */
    int length();
    /**
     * Returns the first position in the path.
 *
     * @return the value defined by this contract
     */
    PathPosition getStart();
    /**
     * Returns the final position in the path.
 *
     * @return the value defined by this contract
     */
    PathPosition getEnd();
    /**
     * Returns the path positions as a collection for consumers that cannot iterate lazily.
 *
     * @return the requested values
     */
    Collection<PathPosition> collect();
}
