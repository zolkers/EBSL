package fr.riege.ebsl.loader.layer;

import fr.riege.ebsl.common.platform.layer.IPhysicsLayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public class MinecraftPhysicsLayer implements IPhysicsLayer {
    private final Minecraft client;

    public MinecraftPhysicsLayer(Minecraft client) {
        this.client = client;
    }

    @Override public void setRotation(float yaw, float pitch) {
        LocalPlayer player = client.player;
        if (player == null) {
            return;
        }
        float clampedPitch = Math.clamp(pitch, -90.0f, 90.0f);
        float prevYaw = player.getYRot();
        float prevPitch = player.getXRot();
        player.setYRot(yaw);
        player.setXRot(clampedPitch);
        player.yRotO = prevYaw;
        player.xRotO = prevPitch;
        player.yHeadRotO = prevYaw;
        player.yBodyRotO = prevYaw;
        player.yHeadRot = yaw;
        player.yBodyRot = yaw;
    }

    @Override public double rotationGcd() {
        double sensitivity = client.options.sensitivity().get();
        double f = sensitivity * 0.6 + 0.2;
        return f * f * f * 1.2;
    }
}
