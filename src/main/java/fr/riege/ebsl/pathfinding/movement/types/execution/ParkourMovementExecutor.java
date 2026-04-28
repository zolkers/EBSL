package fr.riege.ebsl.pathfinding.movement.types.execution;

import fr.riege.ebsl.pathfinding.Node;
import fr.riege.ebsl.pathfinding.movement.types.annotation.MovementHandler;

@MovementHandler(Node.MoveType.PARKOUR)
final class ParkourMovementExecutor implements MovementExecutor {
    private static final double TAKEOFF_EDGE_MARGIN = 0.45;

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
        return Math.max(context.parkourTriggerDistance(), distance - TAKEOFF_EDGE_MARGIN);
    }
}
