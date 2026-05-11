package fr.riege.ebsl.common.platform.layer;

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.common.domain.world.BlockId;

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
    default boolean canRayTraceBlock(Vec3d from, Vec3d to, int targetX, int targetY, int targetZ) {
        int steps = Math.max(1, (int) Math.ceil(from.distanceTo(to) * 8.0));
        for (int i = 1; i <= steps; i++) {
            double t = (double) i / steps;
            int x = (int) Math.floor(from.x() + (to.x() - from.x()) * t);
            int y = (int) Math.floor(from.y() + (to.y() - from.y()) * t);
            int z = (int) Math.floor(from.z() + (to.z() - from.z()) * t);
            if (x == targetX && y == targetY && z == targetZ) {
                return true;
            }
            if (isLoaded(x, y, z) && !isAir(x, y, z) && isSolid(x, y, z)) {
                return false;
            }
        }
        return false;
    }
    default boolean isPartialSupport(int x, int y, int z) {
        double top = getBlockHeight(x, y - 1, z);
        return top > 0.0 && top < 0.95;
    }
    default boolean isSlime(int x, int y, int z) { return false; }
    default boolean isHeadUnderWater(Vec3d eyePosition) {
        return isWater((int) Math.floor(eyePosition.x()), (int) Math.floor(eyePosition.y()), (int) Math.floor(eyePosition.z()));
    }
}
