package fr.riege.ebsl.common.api.navigation;

import fr.riege.ebsl.common.api.core.annotation.EbslApiOperation;
import fr.riege.ebsl.common.api.core.annotation.EbslApiSurface;
import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.goal.NavigationRequest;
import fr.riege.ebsl.common.platform.service.EbslServices;
import fr.riege.ebsl.common.platform.service.NavigationService;

@EbslApiSurface(EbslApiSurface.Domain.NAVIGATION)
public final class NavigationApi {
    @EbslApiOperation("Read the current navigation service.")
    public NavigationService service() {
        return EbslServices.navigation();
    }

    @EbslApiOperation("Read the current navigation snapshot.")
    public NavigationSnapshot snapshot() {
        NavigationService nav = service();
        return new NavigationSnapshot(
            nav.pathStatus(),
            nav.isNavigating(),
            nav.currentMoveType(),
            nav.isWalkSneakLatched(),
            nav.lastPathNodeCount());
    }

    @EbslApiOperation("Start navigation to a block goal.")
    public void startBlockGoal(int x, int y, int z) {
        service().startBlockGoal(x, y, z);
    }

    @EbslApiOperation("Start long-range navigation to an X/Z column.")
    public void startColumnGoal(int x, int z) {
        service().startColumnGoal(x, z);
    }

    @EbslApiOperation("Start a path test without executing the path.")
    public void startPathTest(int x, int y, int z) {
        service().startPathTest(x, y, z);
    }

    @EbslApiOperation("Start a path test to an X/Z column.")
    public void startPathTestXZ(int x, int z) {
        service().startPathTestXZ(x, z);
    }

    @EbslApiOperation("Start navigation from a structured request.")
    public void start(NavigationRequest request) {
        service().startNavigation(request);
    }

    @EbslApiOperation("Stop active navigation.")
    public void stop() {
        service().stop(true);
    }

    @EbslApiOperation("Check whether navigation is active.")
    public boolean isNavigating() {
        return service().isNavigating();
    }

    @EbslApiOperation("Read the currently executed move type.")
    public Node.MoveType currentMoveType() {
        return service().currentMoveType();
    }

    @EbslApiOperation("Start a direct walk used by bot tasks.")
    public void startGreenhouseWalk(Vec3d target, Runnable onFinished, boolean isFirst) {
        service().startGreenhouseWalk(target, onFinished, isFirst);
    }
}
