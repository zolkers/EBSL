package fr.riege.ebsl.common.navigation.runtime.entity;

/**
 * Defines the contract for {@code NavigationMotor} implementations.
 */
public interface NavigationMotor {
    void apply(MovementIntent intent);

    default void stop() {
        apply(MovementIntent.stop());
    }
}
