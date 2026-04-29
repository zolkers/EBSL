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
    PathProximitySnapshot proximity
) {
    public Node.MoveType currentMoveType() {
        if (path == null || path.isEmpty()) {
            return Node.MoveType.WALK;
        }
        int index = Math.max(0, Math.min(pursuitSegment, path.size() - 1));
        return path.get(index).moveType;
    }

    public boolean hasMoveTypeInWindow(Node.MoveType moveType, int lookahead) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        int start = Math.max(0, pursuitSegment);
        int end = Math.min(path.size() - 1, start + Math.max(0, lookahead));
        for (int i = start; i <= end; i++) {
            if (path.get(i).moveType == moveType) {
                return true;
            }
        }
        return false;
    }
}
