package fr.riege.ebsl.pathfinding.pathing.result;

import fr.riege.ebsl.pathfinding.wrapper.PathPosition;
import java.util.Collection;

public interface Path extends Iterable<PathPosition> {
    int length();
    PathPosition getStart();
    PathPosition getEnd();
    Collection<PathPosition> collect();
}
