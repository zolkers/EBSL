package fr.riege.ebsl.common.pathfinding.movement.types.execution;

import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.movement.types.annotation.MovementHandler;

@MovementHandler(Node.MoveType.STEP_UP)
final class StepUpMovementExecutor implements MovementExecutor {
    private static final double MIN_FULL_STEP_TRIGGER_DISTANCE = 1.25;

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
        double triggerDistance = Math.max(context.stepUpTriggerDistance(), MIN_FULL_STEP_TRIGGER_DISTANCE);
        if (context.horizontalDistance() <= triggerDistance) {
            context.pressJump();
        } else {
            context.releaseJump();
        }
    }
}
