package fr.riege.ebsl.common.pathfinding.execution;

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.rotation.EasingType;

import java.util.List;

interface MovementSmoothing {
    boolean applies(List<Node> path, int pursuitSegment);

    Plan plan(List<Node> path, int pursuitSegment, boolean alreadyRotating);

    record Plan(String mode, int targetIndex, int visualizerIndex, Vec3d position,
                float yawThreshold, float pitchThreshold, long durationMs,
                EasingType yawEasing, EasingType pitchEasing) {
    }
}
