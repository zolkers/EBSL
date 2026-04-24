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

    @Override
    public MovementValidationResult validate(MovementValidationContext context) {
        int fromX = context.from().position.flooredX();
        int fromY = context.from().position.flooredY();
        int fromZ = context.from().position.flooredZ();
        int x = context.targetX();
        int y = context.targetY();
        int z = context.targetZ();
        if (!context.checker().safeToFall(fromX, fromY, fromZ, x, y, z)) {
            return MovementValidationResult.invalid("fall segment became unsafe at " + x + ", " + y + ", " + z);
        }
        if (context.checker().isWalkable(x, y, z)
            || context.checker().isWater(x, y, z)
            || context.checker().isClimbable(x, y, z)) {
            return MovementValidationResult.ok();
        }
        return MovementValidationResult.invalid("fall landing is no longer traversable at "
            + x + ", " + y + ", " + z);
    }
}
