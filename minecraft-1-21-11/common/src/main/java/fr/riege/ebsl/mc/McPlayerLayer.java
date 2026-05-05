package fr.riege.ebsl.mc;

import fr.riege.ebsl.common.layer.IPlayerLayer;
import fr.riege.ebsl.common.math.Vec2f;
import fr.riege.ebsl.common.math.Vec3d;
import net.minecraft.client.Minecraft;

public class McPlayerLayer implements IPlayerLayer {
    private final Minecraft client;
    public McPlayerLayer(Minecraft client) { this.client = client; }

    @Override public Vec3d position() { throw new UnsupportedOperationException("TODO"); }
    @Override public Vec2f rotation() { throw new UnsupportedOperationException("TODO"); }
    @Override public Vec3d velocity() { throw new UnsupportedOperationException("TODO"); }
    @Override public boolean isOnGround() { throw new UnsupportedOperationException("TODO"); }
    @Override public boolean isInWater() { throw new UnsupportedOperationException("TODO"); }
    @Override public boolean isInLava() { throw new UnsupportedOperationException("TODO"); }
    @Override public boolean isSprinting() { throw new UnsupportedOperationException("TODO"); }
    @Override public boolean isSneaking() { throw new UnsupportedOperationException("TODO"); }
    @Override public boolean isAlive() { throw new UnsupportedOperationException("TODO"); }
    @Override public float getHealth() { throw new UnsupportedOperationException("TODO"); }
    @Override public int getDimension() { throw new UnsupportedOperationException("TODO"); }
}
