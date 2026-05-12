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
        context.setVerticalControls(true, false);
        context.setSprintPressed(true);
    }
}
