package fr.riege.ebsl.pathfinding.movement.types.execution;

import fr.riege.ebsl.pathfinding.Node;
import fr.riege.ebsl.pathfinding.movement.types.annotation.MovementHandler;

@MovementHandler(Node.MoveType.STEP_UP)
final class StepUpMovementExecutor implements MovementExecutor {
    @Override
    public void handleJump(MovementExecutionContext context) {
        if (context.partialSupportAscent()) {
            context.releaseJump();
            return;
        }
        if (context.verticalDelta() <= 0) {
            context.releaseJump();
            return;
        }
        if (!context.canStartJump()) {
            return;
        }
        if (context.horizontalDistance() < context.stepUpTriggerDistance()) {
            context.pressJump();
        } else {
            context.releaseJump();
        }
    }
}
