package fr.riege.ebsl.loader.layer;

import fr.riege.ebsl.common.platform.layer.IInputLayer;
import net.minecraft.client.Minecraft;

public class MinecraftInputLayer implements IInputLayer {
    protected final Minecraft client;

    public MinecraftInputLayer(Minecraft client) {
        this.client = client;
    }

    @Override
    public void releaseMouse() {
        client.mouseHandler.releaseMouse();
    }

    @Override
    public boolean isMouseGrabbed() {
        return client.mouseHandler.isMouseGrabbed();
    }

    @Override
    public void releaseGameplayKeys() {
        client.options.keyUp.setDown(false);
        client.options.keyDown.setDown(false);
        client.options.keyLeft.setDown(false);
        client.options.keyRight.setDown(false);
        client.options.keyJump.setDown(false);
        client.options.keyShift.setDown(false);
        client.options.keySprint.setDown(false);
        client.options.keyAttack.setDown(false);
        client.options.keyUse.setDown(false);
        client.options.keyPickItem.setDown(false);
    }

    @Override public boolean forwardDown() { return client.options.keyUp.isDown(); }
    @Override public boolean backwardDown() { return client.options.keyDown.isDown(); }
    @Override public boolean leftDown() { return client.options.keyLeft.isDown(); }
    @Override public boolean rightDown() { return client.options.keyRight.isDown(); }
    @Override public boolean jumpDown() { return client.options.keyJump.isDown(); }
    @Override public boolean sneakDown() { return client.options.keyShift.isDown(); }

    @Override public void setForwardDown(boolean down) { client.options.keyUp.setDown(down); }
    @Override public void setBackwardDown(boolean down) { client.options.keyDown.setDown(down); }
    @Override public void setLeftDown(boolean down) { client.options.keyLeft.setDown(down); }
    @Override public void setRightDown(boolean down) { client.options.keyRight.setDown(down); }
    @Override public void setJumpDown(boolean down) { client.options.keyJump.setDown(down); }
    @Override public void setSneakDown(boolean down) { client.options.keyShift.setDown(down); }
    @Override public void setSprintDown(boolean down) { client.options.keySprint.setDown(down); }
    @Override public void setAttackDown(boolean down) { client.options.keyAttack.setDown(down); }
    @Override public void setUseDown(boolean down) { client.options.keyUse.setDown(down); }

    @Override
    public void lookAt(float yaw, float pitch) {
        if (client.player == null) {
            return;
        }
        client.player.setYRot(yaw);
        client.player.setXRot(pitch);
    }
}
