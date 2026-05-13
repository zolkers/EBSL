/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Copyright (C) 2026 Victor Riegert and EBSL contributors
 *
 * This file is part of EBSL.
 *
 * EBSL is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EBSL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EBSL. If not, see <https://www.gnu.org/licenses/>.
 */

package fr.riege.ebsl.loader.layer;

import fr.riege.ebsl.common.platform.layer.IInputLayer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class MinecraftInputLayer implements IInputLayer {
    protected final Minecraft client;
    private BlockPos destroyingBlock;

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
        releaseMovementKeys();
        client.options.keyAttack.setDown(false);
        client.options.keyUse.setDown(false);
        client.options.keyPickItem.setDown(false);
        destroyingBlock = null;
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
    @Override public void setAttackDown(boolean down) {
        client.options.keyAttack.setDown(down);
        if (!down) {
            destroyingBlock = null;
        }
    }
    @Override public void setUseDown(boolean down) { client.options.keyUse.setDown(down); }

    @Override
    public boolean attackTargetedBlock() {
        if (client.player == null || client.gameMode == null || !(client.hitResult instanceof BlockHitResult hit)) {
            return false;
        }
        if (hit.getType() == HitResult.Type.MISS) {
            destroyingBlock = null;
            return false;
        }
        BlockPos pos = hit.getBlockPos();
        boolean handled = pos.equals(destroyingBlock)
            ? client.gameMode.continueDestroyBlock(pos, hit.getDirection())
            : client.gameMode.startDestroyBlock(pos, hit.getDirection());
        destroyingBlock = handled ? pos : null;
        if (handled) {
            client.player.swing(InteractionHand.MAIN_HAND);
        }
        return handled;
    }
}
