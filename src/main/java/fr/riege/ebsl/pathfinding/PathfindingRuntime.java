package fr.riege.ebsl.pathfinding;

import fr.riege.ebsl.pathfinding.execution.FlyExecutor;
import fr.riege.ebsl.pathfinding.execution.PathExecutor;
import net.minecraft.client.Minecraft;

final class PathfindingRuntime {
    final PathExecutor executor = new PathExecutor();
    final FlyExecutor flyExecutor = new FlyExecutor();
    final NavigationState state = new NavigationState();
    final WalkExecutionOptions walkOptions = new WalkExecutionOptions();
    final LongRangePathSession longRangeSession = new LongRangePathSession();

    void abortCurrentNavigation(Minecraft mc) {
        state.abortCurrentNavigation(mc, executor, flyExecutor);
        longRangeSession.clear();
    }
}
