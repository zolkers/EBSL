package fr.riege.ebsl.common.navigation.runtime.entity;

import fr.riege.ebsl.common.math.Vec3d;

public interface NavigationActor {
    Vec3d position();

    default Vec3d velocity() {
        return new Vec3d(0.0, 0.0, 0.0);
    }

    default boolean onGround() {
        return true;
    }

    default boolean isInWater() {
        return false;
    }

    default boolean isInLava() {
        return false;
    }

    default boolean isAlive() {
        return true;
    }

    default double width() {
        return 0.6;
    }

    default double height() {
        return 1.8;
    }
}
