package fr.riege.ebsl.pathfinding.movement.types;

import fr.riege.ebsl.pathfinding.Node;

public final class FlyMovement extends WalkMovement {
    @Override
    public Node.MoveType type() {
        return Node.MoveType.FLY;
    }

    @Override
    public MovementValidationResult validate(MovementValidationContext context) {
        return MovementValidationResult.ok();
    }
}
