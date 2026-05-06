package fr.riege.ebsl.loader.layer;

import fr.riege.ebsl.common.layer.IPhysicsLayer;
import net.minecraft.client.Minecraft;

public class MinecraftPhysicsLayer implements IPhysicsLayer {
    private final Minecraft client;

    public MinecraftPhysicsLayer(Minecraft client) {
        this.client = client;
    }

    @Override public void setSneak(boolean value) { client.options.keyShift.setDown(value); }

    @Override public void clearInputs() {
        client.options.keyUp.setDown(false);
        client.options.keyDown.setDown(false);
        client.options.keyLeft.setDown(false);
        client.options.keyRight.setDown(false);
        client.options.keyJump.setDown(false);
        client.options.keySprint.setDown(false);
        client.options.keyShift.setDown(false);
    }
}
