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

package fr.riege.ebsl.loader.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.systems.RenderSystem;
import fr.riege.ebsl.loader.ModloaderCommonBootstrap;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("java:S107")
@Mixin(LevelRenderer.class)
public final class WorldRendererMixin {
    @Inject(method = "renderLevel", at = @At("RETURN"))
    private void ebslOnRenderLevel(GraphicsResourceAllocator graphicsResourceAllocator,
                                    DeltaTracker deltaTracker,
                                    boolean renderBlockOutline,
                                    Camera camera,
                                    Matrix4f modelView,
                                    Matrix4f projection,
                                    Matrix4f frustumMatrix,
                                    GpuBufferSlice gpuBufferSlice,
                                    Vector4f vector4f,
                                    boolean isTicking,
                                    CallbackInfo ci) {
        RenderSystem.getModelViewStack().pushMatrix().mul(modelView);
        Vec3 position = camera.position();
        ModloaderCommonBootstrap.onRenderWorld(new Matrix4f(projection), position.x, position.y, position.z);
        RenderSystem.getModelViewStack().popMatrix();
    }
}
