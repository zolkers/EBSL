package fr.riege.ebsl.common.pathfinding.pathing.processing;

import fr.riege.ebsl.common.pathfinding.pathing.processing.context.SearchContext;

/**
 * Defines the contract for {@code Processor} implementations.
 */
public interface Processor {
    default void initializeSearch(SearchContext context) {}
    default void finalizeSearch(SearchContext context) {}
}
