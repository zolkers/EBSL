package fr.riege.ebsl.pathfinding.movement.types.execution;

import fr.riege.ebsl.pathfinding.Node;
import fr.riege.ebsl.pathfinding.movement.types.annotation.MovementHandler;

@MovementHandler(Node.MoveType.JUMP)
final class JumpMovementExecutor implements MovementExecutor {
    private static final double PARTIAL_SUPPORT_JUMP_TRIGGER_DISTANCE = 1.35;

    @Override
    public void handleJump(MovementExecutionContext context) {
        if (!context.canStartJump()) {
            return;
        }
        double triggerDistance = context.partialSupportAscent()
            ? Math.max(context.stepUpTriggerDistance(), PARTIAL_SUPPORT_JUMP_TRIGGER_DISTANCE)
            : context.jumpTriggerDistance();
        if (context.horizontalDistance() < triggerDistance) {
            context.pressJump();
        }
    }
}
