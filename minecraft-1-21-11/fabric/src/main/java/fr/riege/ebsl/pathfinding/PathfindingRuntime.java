package fr.riege.ebsl.pathfinding;

import fr.riege.ebsl.pathfinding.annotation.PathStatePersistence;
import fr.riege.ebsl.pathfinding.annotation.PathStateTransition;
import fr.riege.ebsl.pathfinding.annotation.PathingStage;
import fr.riege.ebsl.pathfinding.execution.FlyExecutor;
import fr.riege.ebsl.pathfinding.execution.PathExecutor;
import net.minecraft.client.Minecraft;

@PathingStage(PathingStage.Stage.STATE_PERSISTENCE)
@PathStatePersistence(
    value = PathStatePersistence.Scope.EXECUTION,
    reason = "Owns the in-memory navigation state that may be preserved across internal replans.")
final class PathfindingRuntime {
    @PathStatePersistence(PathStatePersistence.Scope.EXECUTION)
    final PathExecutor executor = new PathExecutor();
    @PathStatePersistence(PathStatePersistence.Scope.EXECUTION)
    final FlyExecutor flyExecutor = new FlyExecutor();
    @PathStatePersistence(PathStatePersistence.Scope.EXECUTION)
    final NavigationState state = new NavigationState();
    @PathStatePersistence(PathStatePersistence.Scope.REQUEST)
    final WalkExecutionOptions walkOptions = new WalkExecutionOptions();
    @PathStatePersistence(PathStatePersistence.Scope.LONG_RANGE_SESSION)
    final LongRangePathSession longRangeSession = new LongRangePathSession();

    @PathStateTransition(PathStateTransition.Action.CLEAR)
    void abortCurrentNavigation(Minecraft mc) {
        abortCurrentNavigation(mc, true);
    }

    @PathStateTransition(
        value = PathStateTransition.Action.PRESERVE,
        reason = "clearLongRange=false keeps the final XZ goal while replacing the active segment.")
    void abortCurrentNavigation(Minecraft mc, boolean clearLongRange) {
        state.abortCurrentNavigation(mc, executor, flyExecutor);
        if (clearLongRange) {
            longRangeSession.clear();
        }
    }
}
