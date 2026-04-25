package fr.riege.ebsl.pathfinding.movement.types;

import fr.riege.ebsl.pathfinding.Node;

public final class ParkourMovement implements PathMovement {
    private static final double TAKEOFF_EDGE_MARGIN = 0.45;

    @Override
    public Node.MoveType type() {
        return Node.MoveType.PARKOUR;
    }

    @Override
    public boolean countsAsAscendingDifficulty() {
        return true;
    }

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
