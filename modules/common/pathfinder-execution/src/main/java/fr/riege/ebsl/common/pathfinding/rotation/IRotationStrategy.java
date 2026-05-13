package fr.riege.ebsl.common.pathfinding.rotation;

import fr.riege.ebsl.common.world.layer.IPlayerLayer;

/**
 * Computes camera rotation updates toward a target orientation.
 *
 * <p>Strategies may keep internal interpolation state between frames and receive explicit start/stop lifecycle callbacks.</p>
 */
public interface IRotationStrategy {
    /**
     * Handles the rotate lifecycle callback.
 *
     * @param player the player abstraction used for the calculation
     * @param targetYaw the yaw angle to rotate toward
     * @param targetPitch the pitch angle to rotate toward
     * @return the value defined by this contract
     */
    Rotation onRotate(IPlayerLayer player, float targetYaw, float targetPitch);

    /**
     * Initializes strategy state when a rotation sequence starts.
 *
     * @param player the player abstraction used for the calculation
     */
    default void onStart(IPlayerLayer player) {
    }

    /**
     * Clears strategy state when a rotation sequence stops.
     */
    default void onStop() {
    }
}
