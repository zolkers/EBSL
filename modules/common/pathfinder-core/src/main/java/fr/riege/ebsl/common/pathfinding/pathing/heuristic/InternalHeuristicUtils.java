package fr.riege.ebsl.common.pathfinding.pathing.heuristic;

import fr.riege.ebsl.common.pathfinding.pathing.PathfindingProgress;

final class InternalHeuristicUtils {
    private static final double EPSILON = 1e-9;

    private InternalHeuristicUtils() {}

    static double calculatePerpendicularDistanceSq(PathfindingProgress progress) {
        double sx = progress.start.centeredX();
        double sy = progress.start.centeredY();
        double sz = progress.start.centeredZ();
        double cx = progress.current.centeredX();
        double cy = progress.current.centeredY();
        double cz = progress.current.centeredZ();
        double tx = progress.target.centeredX();
        double ty = progress.target.centeredY();
        double tz = progress.target.centeredZ();

        double lineX = tx - sx;
        double lineY = ty - sy;
        double lineZ = tz - sz;
        double lineSq = lineX*lineX + lineY*lineY + lineZ*lineZ;

        if (lineSq < EPSILON) {
            double dx = cx - sx;
            double dy = cy - sy;
            double dz = cz - sz;
            return dx*dx + dy*dy + dz*dz;
        }

        double toX = cx - sx;
        double toY = cy - sy;
        double toZ = cz - sz;
        double crossX = toY*lineZ - toZ*lineY;
        double crossY = toZ*lineX - toX*lineZ;
        double crossZ = toX*lineY - toY*lineX;
        double crossSq = crossX*crossX + crossY*crossY + crossZ*crossZ;

        return crossSq / lineSq;
    }

    static double calculatePerpendicularDistance(PathfindingProgress progress) {
        return Math.sqrt(calculatePerpendicularDistanceSq(progress));
    }
}
