package fr.riege.ebsl.common.layer;

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.math.Vec2f;

public interface IPlayerLayer {
    Vec3d position();
    Vec2f rotation();
    Vec3d velocity();
    boolean isOnGround();
    boolean isInWater();
    boolean isInLava();
    boolean isSprinting();
    boolean isSneaking();
    boolean isAlive();
    float getHealth();
    int getDimension();
}
