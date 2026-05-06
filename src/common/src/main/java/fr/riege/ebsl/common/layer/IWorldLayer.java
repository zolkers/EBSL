package fr.riege.ebsl.common.layer;

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
}
