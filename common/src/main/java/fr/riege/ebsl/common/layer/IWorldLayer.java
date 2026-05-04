package fr.riege.ebsl.common.layer;

import fr.riege.ebsl.common.world.BlockId;

public interface IWorldLayer {
    BlockId getBlock(int x, int y, int z);
    boolean isAir(int x, int y, int z);
    boolean isSolid(int x, int y, int z);
    boolean isWater(int x, int y, int z);
    boolean isLava(int x, int y, int z);
    boolean isLoaded(int x, int y, int z);
    boolean isChunkLoaded(int chunkX, int chunkZ);
    int getTopSolidY(int x, int z);
    double getBlockFriction(int x, int y, int z);
    boolean hasOpenTop(int x, int y, int z);
    boolean hasOpenBottom(int x, int y, int z);
    double getBlockHeight(int x, int y, int z);
}
