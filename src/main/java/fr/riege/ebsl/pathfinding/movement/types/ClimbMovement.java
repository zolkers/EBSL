package fr.riege.ebsl.pathfinding.movement.types;

import fr.riege.ebsl.pathfinding.Node;

public final class ClimbMovement extends WalkMovement {
    @Override
    public Node.MoveType type() {
        return Node.MoveType.CLIMB;
    }
}
