package fr.riege.ebsl.common.pathfinding.movement.types.execution;

import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.movement.types.annotation.MovementHandler;

@MovementHandler(Node.MoveType.WALK)
class WalkMovementExecutor implements MovementExecutor {
    @Override
    public void handleJump(MovementExecutionContext context) {
        if (!context.canStartJump()) {
            return;
        }

        boolean blockedStallJump = context.isStalled()
            && context.horizontalDistance() < 1.3
            && !context.inStairSequence();
        boolean stepGapJump = context.verticalDelta() > 0.6
            && context.horizontalDistance() < 2.5
            && !context.partialSupportAscent()
            && !context.inStairSequence();

        if (blockedStallJump || stepGapJump) {
            context.pressJump();
        } else {
            context.releaseJump();
        }
    }
}
