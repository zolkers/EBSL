/*
 * This file is part of EBSL.
 *
 * EBSL is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EBSL is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

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
