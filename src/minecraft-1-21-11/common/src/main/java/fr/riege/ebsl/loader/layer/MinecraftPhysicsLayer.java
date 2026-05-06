package fr.riege.ebsl.loader.layer;

import fr.riege.ebsl.common.layer.IPhysicsLayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public class MinecraftPhysicsLayer implements IPhysicsLayer {
    private final Minecraft client;

    public MinecraftPhysicsLayer(Minecraft client) {
        this.client = client;
    }

    @Override public void lookAt(double x, double y, double z) {
        LocalPlayer player = client.player;
        if (player == null) {
            return;
        }

        double dx = x - player.getX();
        double dy = y - player.getEyeY();
        double dz = z - player.getZ();
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, horizontal));
        setRotation(yaw, pitch);
    }

    @Override public void setForward(boolean value) { client.options.keyUp.setDown(value); }

    @Override public void setBackward(boolean value) { client.options.keyDown.setDown(value); }

    @Override public void setLeft(boolean value) { client.options.keyLeft.setDown(value); }

    @Override public void setRight(boolean value) { client.options.keyRight.setDown(value); }

    @Override public void setJump(boolean value) { client.options.keyJump.setDown(value); }

    @Override public void setSprint(boolean value) { client.options.keySprint.setDown(value); }

    @Override public void setRotation(float yaw, float pitch) {
        LocalPlayer player = client.player;
        if (player == null) {
            return;
        }
        float clampedPitch = Math.max(-90.0f, Math.min(90.0f, pitch));
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
