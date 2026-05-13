package fr.riege.ebsl.common.platform.service;

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.navigation.NavigationStatus;
import fr.riege.ebsl.common.pathfinding.Node;
import fr.riege.ebsl.common.pathfinding.goal.NavigationRequest;

/**
 * Defines the contract for {@code NavigationService} implementations.
 */
public interface NavigationService {
    void startBlockGoal(int x, int y, int z);

    void startColumnGoal(int x, int z);

    void startPathTest(int x, int y, int z);

    void startPathTestXZ(int x, int z);

    void stop(boolean announce);

    default void startNavigation(NavigationRequest request) {
    }

    boolean isNavigating();

    Node.MoveType currentMoveType();

    boolean isWalkSneakLatched();

    void setWalkSneakLatched(boolean value);

    default NavigationStatus pathStatus() {
        return isNavigating() ? NavigationStatus.EXECUTING : NavigationStatus.IDLE;
    }

    default int lastPathNodeCount() {
        return 0;
    }

    default void tick() {
    }

    default void renderCameraFrame() {
        renderWorld();
    }

    default void renderWorld() {
    }

    default void startGreenhouseWalk(Vec3d target, Runnable onFinished, boolean isFirst) {
        startBlockGoal((int) Math.floor(target.x()), (int) Math.floor(target.y()), (int) Math.floor(target.z()));
        if (onFinished != null) {
            onFinished.run();
        }
    }
}
