package fr.riege.ebsl.common.pathfinding.check;

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.pathfinding.Node;

import java.util.List;

public record PathCheckContext(
    Vec3d playerPos,
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
        int index = Math.clamp(pursuitSegment, 0, path.size() - 1);
        return path.get(index).moveType;
    }

    public boolean hasMoveTypeInWindow(Node.MoveType moveType, int lookahead) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        int start = Math.clamp(pursuitSegment, 0, path.size() - 1);
        int end = Math.clamp(start + Math.max(0, lookahead), 0, path.size() - 1);
        for (int i = start; i <= end; i++) {
            if (path.get(i).moveType == moveType) {
                return true;
            }
        }
        return false;
    }
}
