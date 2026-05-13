package fr.riege.ebsl.common.pathfinding.rotation;

import fr.riege.ebsl.common.world.layer.IPlayerLayer;

/**
 * Defines the contract for {@code IRotationStrategy} implementations.
 */
public interface IRotationStrategy {
    Rotation onRotate(IPlayerLayer player, float targetYaw, float targetPitch);

    default void onStart(IPlayerLayer player) {
    }

    default void onStop() {
    }
}
