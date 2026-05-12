package fr.riege.ebsl.common.pathfinding.execution;

import fr.riege.ebsl.common.world.layer.IWorldLayer;
import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;

import java.util.List;

final class PathTargetSelector {
    int pickLegacyCamTarget(IWorldLayer world, Vec3d eyePos, Vec3d playerPos, List<Node> path, int pursuitSegment) {
        int start = Math.clamp(pursuitSegment, 0, path.size() - 1);
        int camTarget = start;
        int lookahead = isParkourWindow(path, pursuitSegment)
            ? 1
            : PathfinderSettings.instance().cameraLookahead.value();
        int camScanEnd = Math.clamp(start + lookahead, 0, path.size() - 1);
        for (int i = camScanEnd; i >= start; i--) {
            if (!isWaypointVisible(world, eyePos, path.get(i))) {
                continue;
            }
            if (i > start && !isStraightLineSafe(path, start, i, playerPos.x(), playerPos.z())) {
                continue;
            }
            if (i > start && hasYChangeBetween(path, start, i)) {
                continue;
            }
            if (i > start && cumulativeTurning(path, start, i) > 45.0) {
                continue;
            }
            camTarget = i;
            break;
        }
        return camTarget;
    }

    private static boolean isParkourWindow(List<Node> path, int pursuitSegment) {
        int start = Math.clamp(pursuitSegment, 0, path.size() - 1);
        int end = Math.clamp(start + 1, 0, path.size() - 1);
        for (int i = start; i <= end; i++) {
            if (path.get(i).moveType == Node.MoveType.PARKOUR) {
                return true;
            }
        }
        return false;
    }

    private static boolean isWaypointVisible(IWorldLayer world, Vec3d eyePos, Node wp) {
        Vec3d target = new Vec3d(wp.position.centeredX(), wp.position.flooredY() + 1.0, wp.position.centeredZ());
        return world.hasLineOfSight(eyePos, target);
    }

    private boolean isStraightLineSafe(List<Node> path, int fromIdx, int toIdx, double px, double pz) {
        Node to = path.get(toIdx);
        double dx = to.position.centeredX() - px;
        double dz = to.position.centeredZ() - pz;
        double lenSq = dx * dx + dz * dz;
        if (lenSq < 0.001) {
            return true;
        }
        double maxPerpSq = PathfinderSettings.instance().cameraMaxLateralDev.value()
            * PathfinderSettings.instance().cameraMaxLateralDev.value();
        for (int j = fromIdx; j < toIdx; j++) {
            Node n = path.get(j);
            double nx = n.position.centeredX() - px;
            double nz = n.position.centeredZ() - pz;
            double cross = nx * dz - nz * dx;
            if ((cross * cross) / lenSq > maxPerpSq) {
                return false;
            }
        }
        return true;
    }

    private static boolean hasYChangeBetween(List<Node> path, int from, int to) {
        int baseY = path.get(from).position.flooredY();
        for (int i = from + 1; i <= to; i++) {
            if (path.get(i).position.flooredY() != baseY) {
                return true;
            }
        }
        return false;
    }

    private static double cumulativeTurning(List<Node> path, int from, int to) {
        if (to - from < 2) {
            return 0;
        }
        double totalAngle = 0;
        for (int i = from + 1; i < to; i++) {
            Node prev = path.get(i - 1);
            Node cur = path.get(i);
            Node next = path.get(i + 1 < path.size() ? i + 1 : i);
            double dx1 = cur.position.centeredX() - prev.position.centeredX();
            double dz1 = cur.position.centeredZ() - prev.position.centeredZ();
            double dx2 = next.position.centeredX() - cur.position.centeredX();
            double dz2 = next.position.centeredZ() - cur.position.centeredZ();
            double len1 = Math.sqrt(dx1 * dx1 + dz1 * dz1);
            double len2 = Math.sqrt(dx2 * dx2 + dz2 * dz2);
            if (len1 > 0.001 && len2 > 0.001) {
                double dot = (dx1 / len1) * (dx2 / len2) + (dz1 / len1) * (dz2 / len2);
                totalAngle += Math.toDegrees(Math.acos(Math.clamp(dot, -1.0, 1.0)));
            }
        }
        return totalAngle;
    }
}
