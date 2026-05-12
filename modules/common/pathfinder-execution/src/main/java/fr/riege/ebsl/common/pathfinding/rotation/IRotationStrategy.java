package fr.riege.ebsl.common.pathfinding.rotation;

import fr.riege.ebsl.common.world.layer.IPlayerLayer;

public interface IRotationStrategy {
    Rotation onRotate(IPlayerLayer player, float targetYaw, float targetPitch);

    default void onStart(IPlayerLayer player) {
    }

    default void onStop() {
    }
}
