package fr.riege.ebsl.common.service;

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.pathfinding.Node;

public interface NavigationService {
    void startBlockGoal(int x, int y, int z);

    void startColumnGoal(int x, int z);

    void startPathTest(int x, int y, int z);

    void startPathTestXZ(int x, int z);

    void stop(boolean announce);

    boolean isNavigating();

    Node.MoveType currentMoveType();

    boolean isWalkSneakLatched();

    void setWalkSneakLatched(boolean value);

    default String pathStatus() {
        return isNavigating() ? "running" : "idle";
    }

    default int lastPathNodeCount() {
        return 0;
    }

    default void tick() {
    }

    default void startGreenhouseWalk(Vec3d target, Runnable onFinished, boolean isFirst) {
        startBlockGoal((int) Math.floor(target.x()), (int) Math.floor(target.y()), (int) Math.floor(target.z()));
        if (onFinished != null) {
            onFinished.run();
        }
    }
}
