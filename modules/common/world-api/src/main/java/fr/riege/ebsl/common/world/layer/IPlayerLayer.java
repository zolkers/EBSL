package fr.riege.ebsl.common.world.layer;

import fr.riege.ebsl.common.domain.world.BlockId;
import fr.riege.ebsl.common.domain.world.TargetedBlock;
import fr.riege.ebsl.common.math.Vec3d;

public interface IPlayerLayer {
    Vec3d position();
    default Vec3d velocity() { return new Vec3d(0.0, 0.0, 0.0); }
    default Vec3d eyePosition() {
        Vec3d position = position();
        return new Vec3d(position.x(), position.y() + 1.62, position.z());
    }
    default float yaw() { return 0.0f; }
    default float pitch() { return 0.0f; }
    default boolean onGround() { return true; }
    default boolean isFlying() { return false; }
    boolean isInWater();
    boolean isInLava();
    boolean isSprinting();
    boolean isAlive();
    float getHealth();
    default TargetedBlock targetedBlockHit() { return null; }
    default BlockId targetedBlock() {
        TargetedBlock target = targetedBlockHit();
        return target == null ? null : target.block();
    }
    default Integer entityId() { return null; }
}
