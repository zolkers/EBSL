package fr.riege.ebsl.common.pathfinding.movement.types.evaluation;

import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.movement.types.annotation.MovementHandler;

@MovementHandler(Node.MoveType.STEP_DOWN)
final class StepDownMovementEvaluator implements MovementEvaluator {
    @Override
    public MovementValidationResult validate(MovementValidationContext context) {
        int x = context.targetX();
        int y = context.targetY();
        int z = context.targetZ();
        if (!context.checker().world().isLoaded(x, y, z)
            || !context.checker().world().isLoaded(x, y + 1, z)) {
            return MovementValidationResult.invalid("step-down target is not loaded at " + x + ", " + y + ", " + z);
        }
        if (context.checker().world().isSolid(x, y + 1, z)) {
            return MovementValidationResult.invalid("step-down headroom blocked at " + x + ", " + (y + 1) + ", " + z);
        }
        if (context.navigationPointProvider()
            .getNavigationPoint(context.target().position, null)
            .isTraversable()) {
            return MovementValidationResult.ok();
        }
        return MovementValidationResult.invalid("step-down landing is no longer traversable at "
            + x + ", " + y + ", " + z);
    }

    @Override
    public boolean reducesSprintNearWaypoint() {
        return true;
    }

    @Override
    public boolean countsAsStairSequence() {
        return true;
    }
}
