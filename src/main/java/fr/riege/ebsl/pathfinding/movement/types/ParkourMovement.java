package fr.riege.ebsl.pathfinding.movement.types;

import fr.riege.ebsl.pathfinding.Node;

public final class ParkourMovement implements PathMovement {
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
        if (context.horizontalDistance() < context.parkourTriggerDistance()) {
            context.pressJump();
        }
    }
}
