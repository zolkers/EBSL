package fr.riege.ebsl.pathfinding.movement.types.evaluation;

import fr.riege.ebsl.pathfinding.Node;
import fr.riege.ebsl.pathfinding.movement.WalkabilityChecker;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

public record MovementValidationContext(
    Minecraft minecraft,
    WalkabilityChecker checker,
    Node from,
    Node target,
    Node next,
    Vec3 playerPos,
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
