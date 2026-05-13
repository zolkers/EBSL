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
package fr.riege.ebsl.loader.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import fr.riege.ebsl.loader.ModloaderCommonBootstrap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderTarget.class)
public final class RenderTargetDockingMixin {
    @Inject(method = "blitToScreen", at = @At("HEAD"))
    private void ebslOnBlitToScreen(CallbackInfo ci) {
        ModloaderCommonBootstrap.onBlitToScreen((RenderTarget) (Object) this);
    }
}
