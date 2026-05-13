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

import com.mojang.blaze3d.platform.Window;
import fr.riege.ebsl.loader.ModloaderCommonBootstrap;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerDockingMixin {
    @Inject(method = "grabMouse", at = @At("HEAD"), cancellable = true)
    private void ebslOnGrabMouse(CallbackInfo ci) {
        if (ModloaderCommonBootstrap.onGrabMouse()) ci.cancel();
    }

    @Inject(method = "onButton", at = @At("HEAD"), cancellable = true)
    private void ebslOnMouseButton(long windowHandle, MouseButtonInfo button, int action, CallbackInfo ci) {
        if (ModloaderCommonBootstrap.onMouseButton(windowHandle, button.button(), action)) ci.cancel();
    }

    @Inject(method = "getScaledXPos(Lcom/mojang/blaze3d/platform/Window;D)D", at = @At("RETURN"), cancellable = true)
    private static void ebslRemapDockedScaledX(Window window, double rawX, CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue(ModloaderCommonBootstrap.remapScaledX(window, rawX, cir.getReturnValueD()));
    }

    @Inject(method = "getScaledYPos(Lcom/mojang/blaze3d/platform/Window;D)D", at = @At("RETURN"), cancellable = true)
    private static void ebslRemapDockedScaledY(Window window, double rawY, CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue(ModloaderCommonBootstrap.remapScaledY(window, rawY, cir.getReturnValueD()));
    }
}
