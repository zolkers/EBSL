package fr.riege.ebsl.common.pathfinding.pathing.result;

import fr.riege.ebsl.common.pathfinding.quality.PathQualityReport;

/**
 * Wraps the outcome of a pathfinding request.
 *
 * <p>Results expose success/failure state, fallback information, the resolved path, and optional quality analysis.</p>
 */
public interface PathfinderResult {
    /**
     * Returns whether pathfinding completed with a usable path.
 *
     * @return true when the condition is satisfied; false otherwise
     */
    boolean successful();
    /**
     * Returns whether pathfinding ended in a failed state.
 *
     * @return true when the condition is satisfied; false otherwise
     */
    boolean hasFailed();
    /**
     * Returns whether the result uses a fallback path rather than the preferred target path.
 *
     * @return true when the condition is satisfied; false otherwise
     */
    boolean hasFallenBack();
    /**
     * Returns the state that best describes the pathfinding outcome.
 *
     * @return the value defined by this contract
     */
    PathState getPathState();
    /**
     * Returns the path produced by the search, when one is available.
 *
     * @return the value defined by this contract
     */
    Path getPath();

    /**
     * Returns the optional quality report attached to this result.
 *
     * @return the value defined by this contract
     */
    default PathQualityReport quality() {
        return PathQualityReport.UNKNOWN;
    }
}
