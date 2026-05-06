package fr.riege.ebsl.common.pathfinding.movement.types.evaluation;

import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.movement.types.annotation.MovementHandler;

@MovementHandler(Node.MoveType.STEP_DOWN)
final class StepDownMovementEvaluator implements MovementEvaluator {
    @Override
    public boolean reducesSprintNearWaypoint() {
        return true;
    }

    @Override
    public boolean countsAsStairSequence() {
        return true;
    }
}
