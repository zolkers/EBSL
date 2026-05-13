package fr.riege.ebsl.common.pathfinding.pathing;

/**
 * Extends the pathfinder contract with diagnostic counters.
 *
 * <p>This interface is intended for tooling, tests, and debug UI that need to inspect how a
 * concrete search behaved without depending on the concrete algorithm class.</p>
 */
public interface InspectablePathfinder extends Pathfinder {
    /**
     * Returns how many nodes were expanded by the most recent search.
     *
     * @return the number of expanded nodes
     */
    long getExploredCount();

    /**
     * Returns a human-readable profiling summary for the most recent search.
     *
     * @return the profiling summary, or a disabled message when profiling is not active
     */
    String getProfilingReport();
}
