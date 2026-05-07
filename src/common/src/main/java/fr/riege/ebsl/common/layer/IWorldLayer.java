package fr.riege.ebsl.common.layer;

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.world.BlockId;

public interface IWorldLayer {
    BlockId getBlock(int x, int y, int z);
    boolean isAir(int x, int y, int z);
    boolean isSolid(int x, int y, int z);
    boolean isWater(int x, int y, int z);
    boolean isLava(int x, int y, int z);
    default boolean isDangerous(int x, int y, int z) { return isLava(x, y, z); }
    default boolean isClimbable(int x, int y, int z) { return false; }
    boolean isLoaded(int x, int y, int z);
    int getTopSolidY(int x, int z);
    double getBlockHeight(int x, int y, int z);
    default boolean requiresJumpForStep(int x, int y, int z, int moveDx, int moveDz) { return false; }
    default boolean hasLineOfSight(Vec3d from, Vec3d to) { return true; }
    default boolean isPartialSupport(int x, int y, int z) {
        double top = getBlockHeight(x, y - 1, z);
        return top > 0.0 && top < 0.95;
    }
    default boolean isSlime(int x, int y, int z) { return false; }
    default boolean isHeadUnderWater(Vec3d eyePosition) {
        return isWater((int) Math.floor(eyePosition.x()), (int) Math.floor(eyePosition.y()), (int) Math.floor(eyePosition.z()));
    }
}
