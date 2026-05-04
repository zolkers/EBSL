package fr.riege.ebsl.pathfinding.execution;

import fr.riege.ebsl.pathfinding.Node;
import fr.riege.ebsl.pathfinding.rotation.EasingType;
import net.minecraft.world.phys.Vec3;

import java.util.List;

interface MovementSmoothing {
    boolean applies(List<Node> path, int pursuitSegment);

    Plan plan(List<Node> path, int pursuitSegment, boolean alreadyRotating);

    record Plan(String mode, int targetIndex, int visualizerIndex, Vec3 position,
                float yawThreshold, float pitchThreshold, long durationMs, EasingType easing) {
    }
}
