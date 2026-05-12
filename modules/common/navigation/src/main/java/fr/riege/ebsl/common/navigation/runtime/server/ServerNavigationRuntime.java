package fr.riege.ebsl.common.navigation.runtime.server;

import fr.riege.ebsl.common.navigation.PathPlannerOptions;
import fr.riege.ebsl.common.navigation.PathPlanningService;
import fr.riege.ebsl.common.navigation.runtime.entity.EntityFollowerOptions;
import fr.riege.ebsl.common.navigation.runtime.entity.EntityNavigationService;
import fr.riege.ebsl.common.navigation.runtime.entity.NavigationActor;
import fr.riege.ebsl.common.navigation.runtime.entity.NavigationMotor;
import fr.riege.ebsl.common.world.layer.IWorldLayer;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public final class ServerNavigationRuntime {
    private final IWorldLayer world;
    private final Consumer<Runnable> callbackThread;
    private PathPlannerOptions plannerOptions = PathPlannerOptions.defaults();
    private EntityFollowerOptions followerOptions = EntityFollowerOptions.defaults();

    private ServerNavigationRuntime(IWorldLayer world, Consumer<Runnable> callbackThread) {
        this.world = Objects.requireNonNull(world, "world");
        this.callbackThread = callbackThread == null ? Runnable::run : callbackThread;
    }

    public static ServerNavigationRuntime direct(IWorldLayer world) {
        return new ServerNavigationRuntime(world, Runnable::run);
    }

    public static ServerNavigationRuntime scheduled(IWorldLayer world, Consumer<Runnable> scheduler) {
        return new ServerNavigationRuntime(world, scheduler);
    }

    public static ServerNavigationRuntime scheduled(IWorldLayer world, Executor executor) {
        Objects.requireNonNull(executor, "executor");
        return new ServerNavigationRuntime(world, executor::execute);
    }

    public ServerNavigationRuntime plannerOptions(PathPlannerOptions options) {
        this.plannerOptions = options == null ? PathPlannerOptions.defaults() : options;
        return this;
    }

    public ServerNavigationRuntime followerOptions(EntityFollowerOptions options) {
        this.followerOptions = options == null ? EntityFollowerOptions.defaults() : options;
        return this;
    }

    public PathPlanningService planner() {
        PathPlanningService planner = new PathPlanningService(world);
        return planner;
    }

    public EntityNavigationService entity(NavigationActor actor, NavigationMotor motor) {
        EntityNavigationService service = new EntityNavigationService(
            new PathPlanningService(world),
            actor,
            motor,
            followerOptions,
            callbackThread);
        service.setPlannerOptions(plannerOptions);
        return service;
    }
}
