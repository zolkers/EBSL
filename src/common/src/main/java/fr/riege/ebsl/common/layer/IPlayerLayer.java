package fr.riege.ebsl.common.layer;

import fr.riege.ebsl.common.math.Vec3d;

public interface IPlayerLayer {
    Vec3d position();
    boolean isInWater();
    boolean isInLava();
    boolean isSprinting();
    boolean isAlive();
    float getHealth();
}
