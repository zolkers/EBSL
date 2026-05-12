package fr.riege.ebsl.common.navigation.runtime.entity;

public interface NavigationMotor {
    void apply(MovementIntent intent);

    default void stop() {
        apply(MovementIntent.stop());
    }
}
