package fr.riege.ebsl.common.pathfinding.pathing.processing;

import fr.riege.ebsl.common.pathfinding.pathing.processing.context.SearchContext;

/**
 * Receives lifecycle callbacks around a pathfinding search.
 *
 * <p>Processors use these hooks to initialize shared data, clear caches, and release state after the search completes.</p>
 */
public interface Processor {
    /**
     * Initializes processor state before a search starts.
 *
     * @param context the context describing the operation being performed
     */
    default void initializeSearch(SearchContext context) {}
    /**
     * Finalizes processor state after a search completes or aborts.
 *
     * @param context the context describing the operation being performed
     */
    default void finalizeSearch(SearchContext context) {}
}
