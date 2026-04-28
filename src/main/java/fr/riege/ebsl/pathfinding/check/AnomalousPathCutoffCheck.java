package fr.riege.ebsl.pathfinding.check;

import fr.riege.ebsl.pathfinding.Node;
import fr.riege.ebsl.pathfinding.annotation.PathCheckRole;

@PathCheckRole("anomalous_cutoff_repair")
final class AnomalousPathCutoffCheck implements PathCheck {
    private static final int MIN_SEGMENT_SKIP = 3;
    private static final double MAX_RECOVERY_CUTOFF_HORIZONTAL_DISTANCE = 5.5;
    private static final double MAX_RECOVERY_CUTOFF_VERTICAL_DISTANCE = 4.5;
    private static final double MIN_NEAREST_ADVANTAGE = 1.25;

    @Override
    public PathCheckResult evaluate(PathCheckContext context) {
        PathProximitySnapshot proximity = context.proximity();
        int candidateSegment = proximity.nearestSegmentIndex();
        if (canCutToBetterNearbySegment(context, proximity, candidateSegment)) {
            return PathCheckResult.repairToSegment(candidateSegment, String.format(
                "anomaly repair segment=%d h=%.2f y=%.2f progress=%.2f",
                candidateSegment,
                proximity.horizontalDistance(),
                proximity.verticalDistance(),
                proximity.progress()));
        }
        return PathCheckResult.none();
    }

    private static boolean canCutToBetterNearbySegment(PathCheckContext context,
                                                       PathProximitySnapshot proximity,
                                                       int candidateSegment) {
        if (candidateSegment < context.pursuitSegment() + MIN_SEGMENT_SKIP) {
            return false;
        }
        if (candidateSegment >= context.path().size() - 1) {
            return false;
        }
        if (proximity.horizontalDistance() > MAX_RECOVERY_CUTOFF_HORIZONTAL_DISTANCE
            || proximity.verticalDistance() > MAX_RECOVERY_CUTOFF_VERTICAL_DISTANCE) {
            return false;
        }

        Node next = context.path().get(Math.min(context.path().size() - 1, context.pursuitSegment() + 1));
        double nextDistance = distanceToNode(context, next);
        return proximity.distance3d() + MIN_NEAREST_ADVANTAGE < nextDistance;
    }

    private static double distanceToNode(PathCheckContext context, Node node) {
        double dx = node.position.centeredX() - context.playerPos().x;
        double dy = node.position.flooredY() - context.playerPos().y;
        double dz = node.position.centeredZ() - context.playerPos().z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

}
