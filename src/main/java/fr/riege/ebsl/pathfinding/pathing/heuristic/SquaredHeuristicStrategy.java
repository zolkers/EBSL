package fr.riege.ebsl.pathfinding.pathing.heuristic;

import fr.riege.ebsl.pathfinding.pathing.PathfindingProgress;
import fr.riege.ebsl.pathfinding.wrapper.PathPosition;

public final class SquaredHeuristicStrategy implements IHeuristicStrategy {
    private static final double D1 = 1.0;
    private static final double D2 = Math.sqrt(2.0);
    private static final double D3 = Math.sqrt(3.0);

    @Override
    public double calculate(HeuristicContext context) {
        PathfindingProgress progress = context.pathfindingProgress;
        HeuristicWeights weights = context.heuristicWeights;

        PathPosition current = progress.current;
        PathPosition target  = progress.target;

        int ax = Math.abs(current.flooredX() - target.flooredX());
        int ay = Math.abs(current.flooredY() - target.flooredY());
        int az = Math.abs(current.flooredZ() - target.flooredZ());

        int manhattan = ax + ay + az;
        double manhattanSq = (double) manhattan * manhattan;

        int min = Math.min(ax, Math.min(ay, az));
        int max = Math.max(ax, Math.max(ay, az));
        int mid = ax + ay + az - min - max;
        double octile = (D3 - D2) * min + (D2 - D1) * mid + D1 * max;
        double octileSq = octile * octile;

        double perpendicularSq = InternalHeuristicUtils.calculatePerpendicularDistanceSq(progress);
        int dy = current.flooredY() - target.flooredY();
        double heightSq = (double) dy * dy;

        return manhattanSq     * weights.manhattanWeight
             + octileSq        * weights.octileWeight
             + perpendicularSq * weights.perpendicularWeight
             + heightSq        * weights.heightWeight;
    }

    @Override
    public double calculateTransitionCost(PathPosition from, PathPosition to) {
        double dx = to.centeredX() - from.centeredX();
        double dy = to.centeredY() - from.centeredY();
        double dz = to.centeredZ() - from.centeredZ();
        return dx*dx + dy*dy + dz*dz;
    }
}
