package fr.riege.ebsl.pathfinding.pathing.processing;

import fr.riege.ebsl.pathfinding.pathing.processing.context.SearchContext;

public interface Processor {
    default void initializeSearch(SearchContext context) {}
    default void finalizeSearch(SearchContext context) {}
}
