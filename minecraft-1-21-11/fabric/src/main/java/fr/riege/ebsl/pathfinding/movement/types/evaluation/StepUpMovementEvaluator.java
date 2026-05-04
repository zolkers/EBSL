package fr.riege.ebsl.pathfinding.movement.types.evaluation;

import fr.riege.ebsl.pathfinding.Node;
import fr.riege.ebsl.pathfinding.movement.types.annotation.MovementHandler;

@MovementHandler(Node.MoveType.STEP_UP)
final class StepUpMovementEvaluator implements MovementEvaluator {
    @Override
    public boolean reducesSprintNearWaypoint() {
        return true;
    }

    @Override
    public boolean countsAsStairSequence() {
        return true;
    }

    @Override
    public boolean countsAsAscendingDifficulty() {
        return true;
    }
}
