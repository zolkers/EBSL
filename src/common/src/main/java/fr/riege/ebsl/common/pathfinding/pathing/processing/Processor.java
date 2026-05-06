package fr.riege.ebsl.common.pathfinding.pathing.processing;

import fr.riege.ebsl.common.pathfinding.pathing.processing.context.SearchContext;

public interface Processor {
    default void initializeSearch(SearchContext context) {}
    default void finalizeSearch(SearchContext context) {}
}
