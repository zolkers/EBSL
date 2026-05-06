package fr.riege.ebsl.common.navigation;

import fr.riege.ebsl.common.layer.IPhysicsLayer;
import fr.riege.ebsl.common.layer.IPlayerLayer;
import fr.riege.ebsl.common.layer.IWorldLayer;
import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.movement.WalkabilityChecker;
import fr.riege.ebsl.common.pathfinding.pathfinder.AStarPathfinder;
import fr.riege.ebsl.common.pathfinding.pathing.configuration.PathfinderConfiguration;
import fr.riege.ebsl.common.pathfinding.pathing.processing.impl.LayerPathProcessor;
import fr.riege.ebsl.common.pathfinding.pathing.result.Path;
import fr.riege.ebsl.common.pathfinding.pathing.result.PathState;
import fr.riege.ebsl.common.pathfinding.pathing.result.PathfinderResult;
import fr.riege.ebsl.common.pathfinding.provider.LayerNavigationPointProvider;
import fr.riege.ebsl.common.pathfinding.settings.PathfinderSettings;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;
import fr.riege.ebsl.common.service.NavigationService;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class CommonNavigationBackend implements NavigationService {
    private final IWorldLayer world;
    private final IPlayerLayer player;
    private final IPhysicsLayer physics;
    private final WalkabilityChecker checker;
    private final LayerNavigationPointProvider provider;

    private AStarPathfinder pathfinder;
    private volatile PathfinderResult lastResult;
    private volatile boolean navigating;
    private volatile boolean sneakLatched;
    private volatile Node.MoveType currentMoveType = Node.MoveType.WALK;
    private Runnable onFinished;

    public CommonNavigationBackend(IWorldLayer world, IPlayerLayer player, IPhysicsLayer physics) {
        this.world = world;
        this.player = player;
        this.physics = physics;
        this.checker = new WalkabilityChecker(world);
        this.provider = new LayerNavigationPointProvider(checker);
    }

    @Override
    public void startBlockGoal(int x, int y, int z) {
        startPathTo(new PathPosition(x, y, z), null);
    }

    @Override
    public void startColumnGoal(int x, int z) {
        startPathTo(new PathPosition(x, world.getTopSolidY(x, z), z), null);
    }

    @Override
    public void startPathTest(int x, int y, int z) {
        startPathTo(new PathPosition(x, y, z), null);
    }

    @Override
    public void startPathTestXZ(int x, int z) {
        startColumnGoal(x, z);
    }

    @Override
    public void stop(boolean announce) {
        navigating = false;
        onFinished = null;
        physics.clearInputs();
        if (pathfinder != null) {
            pathfinder.abort();
        }
    }

    @Override
    public boolean isNavigating() {
        return navigating;
    }

    @Override
    public Node.MoveType currentMoveType() {
        return currentMoveType;
    }

    @Override
    public boolean isWalkSneakLatched() {
        return sneakLatched;
    }

    @Override
    public void setWalkSneakLatched(boolean value) {
        sneakLatched = value;
        physics.setSneak(value);
    }

    @Override
    public void startGreenhouseWalk(Vec3d target, Runnable onFinished, boolean isFirst) {
        startPathTo(new PathPosition(target.x(), target.y(), target.z()), onFinished);
    }

    public PathfinderResult lastResult() {
        return lastResult;
    }

    public Collection<PathPosition> lastPathPositions() {
        Path path = lastResult == null ? null : lastResult.getPath();
        return path == null ? List.of() : path.collect();
    }

    @Override
    public void tick() {
        if (!navigating) {
            return;
        }
        PathfinderResult result = lastResult;
        if (result != null && result.getPathState() == PathState.FOUND) {
            navigating = false;
            physics.clearInputs();
            Runnable finished = onFinished;
            onFinished = null;
            if (finished != null) {
                finished.run();
            }
        }
    }

    private void startPathTo(PathPosition target, Runnable onFinished) {
        checker.clearCache();
        this.onFinished = onFinished;
        navigating = true;
        currentMoveType = Node.MoveType.WALK;

        Vec3d pos = player.position();
        PathPosition start = new PathPosition(pos.x(), pos.y(), pos.z());
        PathfinderConfiguration config = PathfinderConfiguration.builder()
            .maxIterations(PathfinderSettings.instance().defaultWalkMaxIterations.value())
            .maxLength(PathfinderSettings.instance().defaultWalkMaxLength.value())
            .provider(provider)
            .processors(List.of(new LayerPathProcessor()))
            .build();
        pathfinder = new AStarPathfinder(config);

        CompletableFuture
            .supplyAsync(() -> pathfinder.findPath(start, target).toCompletableFuture().join())
            .whenComplete((result, throwable) -> {
                if (throwable != null) {
                    navigating = false;
                    lastResult = null;
                    return;
                }
                lastResult = result;
                if (result == null || result.hasFailed()) {
                    navigating = false;
                    physics.clearInputs();
                }
            });
    }
}
