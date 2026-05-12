package fr.riege.ebsl.common.pathfinding.movement.types.execution;

import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.movement.types.annotation.MovementHandler;

@MovementHandler(Node.MoveType.CLIMB)
final class ClimbMovementExecutor extends WalkMovementExecutor {
}
