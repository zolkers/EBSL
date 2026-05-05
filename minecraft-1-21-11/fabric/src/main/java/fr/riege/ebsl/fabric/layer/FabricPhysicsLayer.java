package fr.riege.ebsl.fabric.layer;

import fr.riege.ebsl.common.layer.IPhysicsLayer;
import net.minecraft.client.Minecraft;

public class FabricPhysicsLayer implements IPhysicsLayer {
    private final Minecraft client;
    public FabricPhysicsLayer(Minecraft client) { this.client = client; }

    @Override public void setForward(float value) { throw new UnsupportedOperationException("TODO"); }
    @Override public void setSideways(float value) { throw new UnsupportedOperationException("TODO"); }
    @Override public void setJump(boolean value) { throw new UnsupportedOperationException("TODO"); }
    @Override public void setSprint(boolean value) { throw new UnsupportedOperationException("TODO"); }
    @Override public void setSneak(boolean value) { throw new UnsupportedOperationException("TODO"); }
    @Override public void setYaw(float yaw) { throw new UnsupportedOperationException("TODO"); }
    @Override public void setPitch(float pitch) { throw new UnsupportedOperationException("TODO"); }
    @Override public void clearInputs() { throw new UnsupportedOperationException("TODO"); }
}
