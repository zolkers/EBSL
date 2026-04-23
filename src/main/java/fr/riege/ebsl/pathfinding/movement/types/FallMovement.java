package fr.riege.ebsl.pathfinding.movement.types;

import fr.riege.ebsl.pathfinding.Node;

public final class FallMovement extends WalkMovement {
    @Override
    public Node.MoveType type() {
        return Node.MoveType.FALL;
    }

    @Override
    public boolean countsAsStairSequence() {
        return true;
    }
}
