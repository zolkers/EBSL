package fr.riege.ebsl.common.navigation.runtime.entity;

/**
 * Applies movement intents produced by navigation runtime code.
 *
 * <p>Motors translate high-level intent into platform-specific key, velocity, or simulation commands.</p>
 */
public interface NavigationMotor {
    /**
     * Applies this operation to the supplied value or context.
 *
     * @param intent the intent value
     */
    void apply(MovementIntent intent);

    /**
     * Stops the active operation and releases active controls when appropriate.
     */
    default void stop() {
        apply(MovementIntent.stop());
    }
}
