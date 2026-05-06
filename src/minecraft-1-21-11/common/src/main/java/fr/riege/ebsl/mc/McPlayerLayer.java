package fr.riege.ebsl.mc;

import fr.riege.ebsl.common.layer.IPlayerLayer;
import fr.riege.ebsl.common.math.Vec3d;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public class McPlayerLayer implements IPlayerLayer {
    private final Minecraft client;
    public McPlayerLayer(Minecraft client) { this.client = client; }

    @Override public Vec3d position() {
        LocalPlayer player = player();
        return player == null ? zero() : new Vec3d(player.getX(), player.getY(), player.getZ());
    }

    @Override public boolean isInWater() {
        LocalPlayer player = player();
        return player != null && player.isInWater();
    }

    @Override public boolean isInLava() {
        LocalPlayer player = player();
        return player != null && player.isInLava();
    }

    @Override public boolean isSprinting() {
        LocalPlayer player = player();
        return player != null && player.isSprinting();
    }

    @Override public boolean isAlive() {
        LocalPlayer player = player();
        return player != null && player.isAlive();
    }

    @Override public float getHealth() {
        LocalPlayer player = player();
        return player == null ? 0.0f : player.getHealth();
    }

    private LocalPlayer player() {
        return client.player;
    }

    private static Vec3d zero() {
        return new Vec3d(0.0, 0.0, 0.0);
    }
}
