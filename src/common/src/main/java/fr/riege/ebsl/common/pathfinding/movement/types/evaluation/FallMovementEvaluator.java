package fr.riege.ebsl.common.pathfinding.movement.types.evaluation;

import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.movement.types.annotation.MovementHandler;

@MovementHandler(Node.MoveType.FALL)
final class FallMovementEvaluator extends WalkMovementEvaluator {
    @Override
    public MovementValidationResult validate(MovementValidationContext context) {
        int fromY = context.from().position.flooredY();
        int x = context.targetX();
        int y = context.targetY();
        int z = context.targetZ();
        if (!context.checker().safeToFall(fromY, x, y, z)) {
            return MovementValidationResult.invalid("fall segment became unsafe at " + x + ", " + y + ", " + z);
        }
        if (context.checker().isWalkable(x, y, z) || context.checker().isWater(x, y, z)) {
            return MovementValidationResult.ok();
        }
        return MovementValidationResult.invalid("fall landing is no longer traversable at "
            + x + ", " + y + ", " + z);
    }
}
