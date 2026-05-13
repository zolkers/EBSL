package fr.riege.ebsl.common.pathfinding.execution;

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.rotation.EasingType;

import java.util.List;

/**
 * Chooses smoothed pursuit targets for path execution.
 *
 * <p>Smoothing strategies inspect the upcoming path segment and produce a target plan that can reduce jitter without hiding execution intent.</p>
 */
interface MovementSmoothing {
    /**
     * Returns whether this smoothing strategy applies to the supplied path segment.
 *
     * @param path the path or file path to use
     * @param pursuitSegment the pursuit segment value
     * @return true when the condition is satisfied; false otherwise
     */
    boolean applies(List<Node> path, int pursuitSegment);

    /**
     * Returns the smoothing plan to use for the supplied pursuit segment.
 *
     * @param path the path or file path to use
     * @param pursuitSegment the pursuit segment value
     * @param alreadyRotating the already rotating value
     * @return the value defined by this contract
     */
    Plan plan(List<Node> path, int pursuitSegment, boolean alreadyRotating);

    record Plan(String mode, int targetIndex, int visualizerIndex, Vec3d position,
                float yawThreshold, float pitchThreshold, long durationMs,
                EasingType yawEasing, EasingType pitchEasing) {
    }
}
