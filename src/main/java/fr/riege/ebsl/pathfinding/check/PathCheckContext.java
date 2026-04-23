package fr.riege.ebsl.pathfinding.check;

import fr.riege.ebsl.pathfinding.Node;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public record PathCheckContext(
    Vec3 playerPos,
    List<Node> path,
    int pursuitSegment,
    int goalX,
    int goalY,
    int goalZ,
    long severeOffPathDurationMs,
    PathProximitySnapshot proximity,
    PathCheckpointSnapshot checkpoint
) {
}
