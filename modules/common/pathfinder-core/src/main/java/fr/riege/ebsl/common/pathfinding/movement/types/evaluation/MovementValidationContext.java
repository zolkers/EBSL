package fr.riege.ebsl.common.pathfinding.movement.types.evaluation;

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.movement.WalkabilityChecker;
import fr.riege.ebsl.common.pathfinding.provider.NavigationPointProvider;

public record MovementValidationContext(
    WalkabilityChecker checker,
    NavigationPointProvider navigationPointProvider,
    Node from,
    Node target,
    Node next,
    Vec3d playerPos,
    int pursuitSegment
) {
    public int targetX() {
        return target.position.flooredX();
    }

    public int targetY() {
        return target.position.flooredY();
    }

    public int targetZ() {
        return target.position.flooredZ();
    }
}
