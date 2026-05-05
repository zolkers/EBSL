package fr.riege.ebsl.mc;

import fr.riege.ebsl.common.layer.IWorldLayer;
import fr.riege.ebsl.common.world.BlockId;
import net.minecraft.client.Minecraft;

public class McWorldLayer implements IWorldLayer {
    private final Minecraft client;
    public McWorldLayer(Minecraft client) { this.client = client; }

    @Override public BlockId getBlock(int x, int y, int z) { throw new UnsupportedOperationException("TODO"); }
    @Override public boolean isAir(int x, int y, int z) { throw new UnsupportedOperationException("TODO"); }
    @Override public boolean isSolid(int x, int y, int z) { throw new UnsupportedOperationException("TODO"); }
    @Override public boolean isWater(int x, int y, int z) { throw new UnsupportedOperationException("TODO"); }
    @Override public boolean isLava(int x, int y, int z) { throw new UnsupportedOperationException("TODO"); }
    @Override public boolean isLoaded(int x, int y, int z) { throw new UnsupportedOperationException("TODO"); }
    @Override public boolean isChunkLoaded(int cx, int cz) { throw new UnsupportedOperationException("TODO"); }
    @Override public int getTopSolidY(int x, int z) { throw new UnsupportedOperationException("TODO"); }
    @Override public double getBlockFriction(int x, int y, int z) { throw new UnsupportedOperationException("TODO"); }
    @Override public boolean hasOpenTop(int x, int y, int z) { throw new UnsupportedOperationException("TODO"); }
    @Override public boolean hasOpenBottom(int x, int y, int z) { throw new UnsupportedOperationException("TODO"); }
    @Override public double getBlockHeight(int x, int y, int z) { throw new UnsupportedOperationException("TODO"); }
}
