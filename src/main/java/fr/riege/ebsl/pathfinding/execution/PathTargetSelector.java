package fr.riege.ebsl.pathfinding.execution;

import fr.riege.ebsl.pathfinding.Node;
import fr.riege.ebsl.util.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

import java.util.List;

final class PathTargetSelector {
    private static final int CAMERA_LOOKAHEAD = 32;
    private static final double CAM_MAX_LATERAL_DEV = 2.5;

    int pickLegacyCamTarget(Minecraft mc, Vec3 playerPos, List<Node> path, int pursuitSegment) {
        int camTarget = pursuitSegment;
        int camScanEnd = Math.min(path.size() - 1, pursuitSegment + CAMERA_LOOKAHEAD);
        for (int i = camScanEnd; i >= pursuitSegment; i--) {
            if (!isWaypointVisible(mc, path.get(i))) {
                continue;
            }
            if (i > pursuitSegment && !isStraightLineSafe(path, pursuitSegment, i, playerPos.x, playerPos.z)) {
                continue;
            }
            if (i > pursuitSegment && hasYChangeBetween(path, pursuitSegment, i)) {
                continue;
            }
            if (i > pursuitSegment && cumulativeTurning(path, pursuitSegment, i) > 45.0) {
                continue;
            }
            camTarget = i;
            break;
        }
        return camTarget;
    }

    private static boolean isWaypointVisible(Minecraft mc, Node wp) {
        if (mc.player == null) {
            return false;
        }
        Vec3 target = new Vec3(
            wp.position.centeredX(),
            wp.position.flooredY() + 1.0,
            wp.position.centeredZ());
        return ClientUtils.hasLineOfSight(mc.player, target);
    }

    private boolean isStraightLineSafe(List<Node> path, int fromIdx, int toIdx, double px, double pz) {
        Node to = path.get(toIdx);
        double dx = to.position.centeredX() - px;
        double dz = to.position.centeredZ() - pz;
        double lenSq = dx * dx + dz * dz;
        if (lenSq < 0.001) {
            return true;
        }
        double maxPerpSq = CAM_MAX_LATERAL_DEV * CAM_MAX_LATERAL_DEV;
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
                totalAngle += Math.toDegrees(Math.acos(Math.max(-1, Math.min(1, dot))));
            }
        }
        return totalAngle;
    }
}
