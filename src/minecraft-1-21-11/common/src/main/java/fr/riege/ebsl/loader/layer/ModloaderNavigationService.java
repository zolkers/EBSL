package fr.riege.ebsl.loader.layer;

import fr.riege.ebsl.common.platform.layer.IPhysicsLayer;
import fr.riege.ebsl.common.platform.layer.IPlayerLayer;
import fr.riege.ebsl.common.platform.layer.IWorldLayer;
import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.navigation.CommonNavigationBackend;
import fr.riege.ebsl.common.navigation.NavigationStatus;
import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.goal.NavigationRequest;
import fr.riege.ebsl.common.platform.service.NavigationService;
import net.minecraft.client.Minecraft;

public final class ModloaderNavigationService implements NavigationService {
    private final CommonNavigationBackend backend;

    public ModloaderNavigationService(IWorldLayer world, IPlayerLayer player, IPhysicsLayer physics) {
        this.backend = new CommonNavigationBackend(world, player, physics, Minecraft.getInstance()::execute);
    }

    @Override public void startBlockGoal(int x, int y, int z) { backend.startBlockGoal(x, y, z); }
    @Override public void startColumnGoal(int x, int z) { backend.startColumnGoal(x, z); }
    @Override public void startPathTest(int x, int y, int z) { backend.startPathTest(x, y, z); }
    @Override public void startPathTestXZ(int x, int z) { backend.startPathTestXZ(x, z); }
    @Override public void stop(boolean announce) { backend.stop(announce); }
    @Override public void startNavigation(NavigationRequest request) { backend.startNavigation(request); }
    @Override public boolean isNavigating() { return backend.isNavigating(); }
    @Override public Node.MoveType currentMoveType() { return backend.currentMoveType(); }
    @Override public boolean isWalkSneakLatched() { return backend.isWalkSneakLatched(); }
    @Override public void setWalkSneakLatched(boolean value) { backend.setWalkSneakLatched(value); }
    @Override public NavigationStatus pathStatus() { return backend.pathStatus(); }
    @Override public int lastPathNodeCount() { return backend.lastPathNodeCount(); }
    @Override public void startGreenhouseWalk(Vec3d target, Runnable onFinished, boolean isFirst) {
        backend.startGreenhouseWalk(target, onFinished, isFirst);
    }

    @Override
    public void tick() {
        backend.tick();
    }

    @Override
    public void renderCameraFrame() {
        backend.renderCameraFrame();
    }

    @Override
    public void renderWorld() {
        backend.renderWorld();
    }
}
