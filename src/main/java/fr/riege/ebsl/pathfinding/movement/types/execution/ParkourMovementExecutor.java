package fr.riege.ebsl.pathfinding.movement.types.execution;

import fr.riege.ebsl.pathfinding.Node;
import fr.riege.ebsl.pathfinding.movement.types.annotation.MovementHandler;

@MovementHandler(Node.MoveType.PARKOUR)
final class ParkourMovementExecutor implements MovementExecutor {
    private static final double SHORT_GAP_TAKEOFF_MARGIN = 0.12;
    private static final double MEDIUM_GAP_TAKEOFF_MARGIN = 0.30;
    private static final double LONG_GAP_TAKEOFF_MARGIN = 0.45;

    @Override
    public void handleJump(MovementExecutionContext context) {
        if (!context.canStartJump()) {
            return;
        }
        if (context.horizontalDistance() <= jumpTriggerDistance(context)) {
            context.pressJump();
        }
    }

    private double jumpTriggerDistance(MovementExecutionContext context) {
        int distance = context.parkourDistanceBlocks();
        double margin = distance <= 2
            ? SHORT_GAP_TAKEOFF_MARGIN
            : (distance == 3 ? MEDIUM_GAP_TAKEOFF_MARGIN : LONG_GAP_TAKEOFF_MARGIN);
        return Math.max(context.parkourTriggerDistance(), distance - margin);
    }
}
