package fr.riege.ebsl.pathfinding;

import fr.riege.ebsl.pathfinding.execution.FlyExecutor;
import fr.riege.ebsl.pathfinding.execution.PathExecutor;
import fr.riege.ebsl.pathfinding.pathfinder.AStarPathfinder;
import net.minecraft.client.Minecraft;

import java.util.concurrent.atomic.AtomicBoolean;

final class NavigationState {
    private final AtomicBoolean abortFlag = new AtomicBoolean(false);

    private volatile boolean navigating = false;
    private volatile int goalX;
    private volatile int goalY;
    private volatile int goalZ;
    private volatile NavigationMode activeMode = NavigationMode.NONE;
    private volatile AStarPathfinder currentPathfinder;

    void begin(NavigationMode mode, int goalX, int goalY, int goalZ) {
        abortFlag.set(false);
        navigating = true;
        activeMode = mode;
        this.goalX = goalX;
        this.goalY = goalY;
        this.goalZ = goalZ;
    }

    void updateGoal(int goalX, int goalY, int goalZ) {
        this.goalX = goalX;
        this.goalY = goalY;
        this.goalZ = goalZ;
    }

    void markIdle() {
        navigating = false;
        activeMode = NavigationMode.NONE;
    }

    void abortCurrentNavigation(Minecraft mc, PathExecutor executor, FlyExecutor flyExecutor) {
        abortFlag.set(true);
        AStarPathfinder walkPathfinder = currentPathfinder;
        if (walkPathfinder != null) {
            walkPathfinder.abort();
        }
        executor.stop(mc);
        flyExecutor.stop(mc);
        currentPathfinder = null;
        markIdle();
    }

    boolean shouldRestartWalkPathfind(PathExecutor executor) {
        return activeMode == NavigationMode.WALK && executor.getState() == PathExecutor.State.REPLANNING;
    }

    boolean isFlyExecutionActive(FlyExecutor flyExecutor) {
        FlyExecutor.State flyState = flyExecutor.getState();
        return activeMode == NavigationMode.FLY
            && (flyState == FlyExecutor.State.FLYING || flyState == FlyExecutor.State.DECELERATING);
    }

    boolean isNavigating() {
        return navigating;
    }

    boolean isAborted() {
        return abortFlag.get();
    }

    NavigationMode activeMode() {
        return activeMode;
    }

    int goalX() {
        return goalX;
    }

    int goalY() {
        return goalY;
    }

    int goalZ() {
        return goalZ;
    }

    void setCurrentPathfinder(AStarPathfinder currentPathfinder) {
        this.currentPathfinder = currentPathfinder;
    }

    void clearCurrentPathfinder() {
        currentPathfinder = null;
    }

    boolean isCurrentPathfinder(AStarPathfinder pathfinder) {
        return currentPathfinder == pathfinder;
    }

}
