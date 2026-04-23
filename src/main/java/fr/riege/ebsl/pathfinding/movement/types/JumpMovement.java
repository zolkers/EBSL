package fr.riege.ebsl.pathfinding.movement.types;

import fr.riege.ebsl.pathfinding.Node;

public final class JumpMovement implements PathMovement {
    @Override
    public Node.MoveType type() {
        return Node.MoveType.JUMP;
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
        if (context.horizontalDistance() < context.jumpTriggerDistance()) {
            context.pressJump();
        }
    }
}
