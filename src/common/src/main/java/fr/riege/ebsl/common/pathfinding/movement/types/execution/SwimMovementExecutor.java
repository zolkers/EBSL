package fr.riege.ebsl.common.pathfinding.movement.types.execution;

import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.movement.types.annotation.MovementHandler;

@MovementHandler(Node.MoveType.SWIM)
final class SwimMovementExecutor extends WalkMovementExecutor {
    @Override
    public void handleJump(MovementExecutionContext context) {
        context.releaseJump();
    }

    @Override
    public void handleWaterMovement(WaterMovementContext context) {
        if (!context.playerInWater()) {
            return;
        }
        int waypointY = context.waypoint().position.flooredY();
        int playerY = (int) Math.floor(context.playerPos().y());
        boolean needUp = waypointY >= playerY || context.headUnderWater();
        boolean needDown = waypointY < playerY - 1;

        context.setVerticalControls(needUp && !needDown, needDown && !needUp);
        context.setSprintPressed(true);
    }
}
