package fr.riege.ebsl.common.navigation.runtime.entity;

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.navigation.NavigationStatus;
import fr.riege.ebsl.common.navigation.PathPlan;
import fr.riege.ebsl.common.navigation.PathPlannerOptions;
import fr.riege.ebsl.common.navigation.PathPlanningService;
import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.goal.NavigationRequest;
import fr.riege.ebsl.common.pathfinding.goal.NavigationTarget;
import fr.riege.ebsl.common.pathfinding.wrapper.PathPosition;
import fr.riege.ebsl.common.platform.service.NavigationService;

import java.util.Objects;
import java.util.function.Consumer;

public class EntityNavigationService implements NavigationService {
    private final PathPlanningService planner;
    private final NavigationActor actor;
    private final EntityPathFollower follower;
    private final Consumer<Runnable> callbackThread;

    private volatile NavigationStatus status = NavigationStatus.IDLE;
    private volatile PathPlan lastPlan;
    private PathPlannerOptions options = PathPlannerOptions.defaults();
    private Runnable onFinished;
    private Runnable onFailed;

    public EntityNavigationService(PathPlanningService planner, NavigationActor actor, NavigationMotor motor) {
        this(planner, actor, motor, EntityFollowerOptions.defaults(), Runnable::run);
    }

    public EntityNavigationService(PathPlanningService planner,
                                   NavigationActor actor,
                                   NavigationMotor motor,
                                   EntityFollowerOptions followerOptions,
                                   Consumer<Runnable> callbackThread) {
        this.planner = Objects.requireNonNull(planner, "planner");
        this.actor = Objects.requireNonNull(actor, "actor");
        this.follower = new EntityPathFollower(actor, motor, followerOptions);
        this.callbackThread = callbackThread == null ? Runnable::run : callbackThread;
    }

    public PathPlan lastPlan() {
        return lastPlan;
    }

    public void setPlannerOptions(PathPlannerOptions options) {
        this.options = options == null ? PathPlannerOptions.defaults() : options;
    }

    @Override public void startNavigation(NavigationRequest request) {
        configure(request);
        Vec3d pos = actor.position();
        int px = (int) Math.floor(pos.x());
        int py = (int) Math.floor(pos.y());
        int pz = (int) Math.floor(pos.z());
        switch (request.goal().resolve(px, py, pz)) {
            case NavigationTarget.Block(int x, int y, int z) -> startBlockGoalConfigured(x, y, z);
            case NavigationTarget.Column(int x, int z) -> startColumnGoalConfigured(x, z);
        }
    }

    @Override public void startBlockGoal(int x, int y, int z) {
        onFinished = null;
        onFailed = null;
        startBlockGoalConfigured(x, y, z);
    }

    @Override public void startColumnGoal(int x, int z) {
        onFinished = null;
        onFailed = null;
        startColumnGoalConfigured(x, z);
    }

    @Override public void startPathTest(int x, int y, int z) {
        startBlockGoal(x, y, z);
    }

    @Override public void startPathTestXZ(int x, int z) {
        Vec3d pos = actor.position();
        startBlockGoal(x, planner.resolveGoalYForXZ(x, (int) Math.floor(pos.y()), z), z);
    }

    @Override public void stop(boolean announce) {
        status = NavigationStatus.IDLE;
        follower.stop();
    }

    @Override public boolean isNavigating() {
        return status == NavigationStatus.CALCULATING || status == NavigationStatus.EXECUTING;
    }

    @Override public Node.MoveType currentMoveType() {
        return follower.currentMoveType();
    }

    @Override public boolean isWalkSneakLatched() {
        return false;
    }

    @Override public void setWalkSneakLatched(boolean value) {
        // Entity navigation has no physical sneak key to latch.
    }

    @Override public NavigationStatus pathStatus() {
        return status == NavigationStatus.EXECUTING ? follower.status() : status;
    }

    @Override public int lastPathNodeCount() {
        return lastPlan == null ? 0 : lastPlan.navigationNodes().size();
    }

    @Override public void tick() {
        follower.tick();
        if (status == NavigationStatus.EXECUTING && follower.status() != NavigationStatus.EXECUTING) {
            status = follower.status();
        }
    }

    private void configure(NavigationRequest request) {
        onFinished = request.onFinished();
        onFailed = request.onFailed();
        options = options.toBuilder()
            .allowParkour(request.allowParkour())
            .allowJump(request.allowJump())
            .allowFall(request.allowFall())
            .allowWalkDiagonal(request.allowWalkDiagonal())
            .build();
    }

    private void startColumnGoalConfigured(int x, int z) {
        Vec3d pos = actor.position();
        int y = planner.resolveGoalYForXZ(x, (int) Math.floor(pos.y()), z);
        startBlockGoalConfigured(x, y, z);
    }

    private void startBlockGoalConfigured(int x, int y, int z) {
        status = NavigationStatus.CALCULATING;
        follower.stop();
        PathPosition start = planner.positionFromEntity(actor.position().x(), actor.position().y(), actor.position().z());
        PathPosition target = new PathPosition(x, y, z);
        planner.plan(start, target, options).whenComplete((plan, throwable) -> callbackThread.accept(() -> {
            if (throwable != null || plan == null || !plan.usable()) {
                lastPlan = plan;
                status = NavigationStatus.FAILED;
                if (onFailed != null) onFailed.run();
                return;
            }
            lastPlan = plan;
            status = NavigationStatus.EXECUTING;
            follower.start(plan, onFinished, onFailed);
        }));
    }
}
