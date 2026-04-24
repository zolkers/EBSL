package fr.riege.ebsl.pathfinding.movement.types;

import fr.riege.ebsl.pathfinding.Node;

public final class StepUpMovement implements PathMovement {
    @Override
    public Node.MoveType type() {
        return Node.MoveType.STEP_UP;
    }

    @Override
    public boolean reducesSprintNearWaypoint() {
        return true;
    }

    @Override
    public boolean countsAsStairSequence() {
        return true;
    }

    @Override
    public boolean countsAsAscendingDifficulty() {
        return true;
    }

    @Override
    public void handleJump(MovementExecutionContext context) {
        if (context.partialSupportAscent()) {
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
