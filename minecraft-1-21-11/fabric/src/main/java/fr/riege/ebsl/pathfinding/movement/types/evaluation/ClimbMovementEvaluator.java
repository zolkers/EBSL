package fr.riege.ebsl.pathfinding.movement.types.evaluation;

import fr.riege.ebsl.pathfinding.Node;
import fr.riege.ebsl.pathfinding.movement.types.annotation.MovementHandler;

@MovementHandler(Node.MoveType.CLIMB)
final class ClimbMovementEvaluator extends WalkMovementEvaluator {
    @Override
    public MovementValidationResult validate(MovementValidationContext context) {
        int x = context.targetX();
        int y = context.targetY();
        int z = context.targetZ();
        if (context.checker().isClimbable(x, y, z)
            || context.checker().isClimbable(context.from().position.flooredX(), context.from().position.flooredY(), context.from().position.flooredZ())) {
            return MovementValidationResult.ok();
        }
        return MovementValidationResult.invalid("climb segment lost climbable blocks near "
            + x + ", " + y + ", " + z);
    }
}
