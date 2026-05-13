package fr.riege.ebsl.common.pathfinding.pathing.result;

import fr.riege.ebsl.common.pathfinding.result.PathImpl;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;

import java.util.Collection;
import java.util.Objects;

/**
 * Creates path instances without exposing their implementation class.
 */
public final class Paths {
    private Paths() {
    }

    /**
     * Creates an immutable path from the supplied endpoints and positions.
     *
     * @param start the requested start position
     * @param end the requested end position
     * @param positions the ordered path positions
     * @return a path view over the supplied positions
     */
    public static Path of(PathPosition start, PathPosition end, Collection<PathPosition> positions) {
        return new PathImpl(
            Objects.requireNonNull(start, "start"),
            Objects.requireNonNull(end, "end"),
            Objects.requireNonNull(positions, "positions"));
    }
}
