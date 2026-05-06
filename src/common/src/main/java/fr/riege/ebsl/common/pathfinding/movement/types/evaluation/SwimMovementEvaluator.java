package fr.riege.ebsl.common.pathfinding.movement.types.evaluation;

import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.movement.types.annotation.MovementHandler;

@MovementHandler(Node.MoveType.SWIM)
final class SwimMovementEvaluator extends WalkMovementEvaluator {
    @Override
    public MovementValidationResult validate(MovementValidationContext context) {
        int x = context.targetX();
        int y = context.targetY();
        int z = context.targetZ();
        if (context.checker().isWater(x, y, z)
            || context.checker().isWater(x, y - 1, z)
            || context.checker().isWalkable(x, y, z)) {
            return MovementValidationResult.ok();
        }
        return MovementValidationResult.invalid("swim segment lost water/exit at " + x + ", " + y + ", " + z);
    }
}
