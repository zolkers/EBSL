package fr.riege.ebsl.mixin;

import com.mojang.blaze3d.platform.Window;
import fr.riege.ebsl.EbslMod;
import fr.riege.ebsl.event.events.input.GrabMouseEvent;
import fr.riege.ebsl.event.events.input.MouseButtonEvent;
import fr.riege.ebsl.event.events.input.ScaledMousePosEvent;
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
    private void ebsl$onGrabMouse(CallbackInfo ci) {
        if (EbslMod.postClientEvent(new GrabMouseEvent()).isCancelled()) ci.cancel();
    }

    @Inject(method = "onButton", at = @At("HEAD"), cancellable = true)
    private void ebsl$onMouseButton(long windowHandle, MouseButtonInfo button, int action, CallbackInfo ci) {
        if (EbslMod.postClientEvent(new MouseButtonEvent(windowHandle, button, action)).isCancelled()) ci.cancel();
    }

    @Inject(method = "getScaledXPos(Lcom/mojang/blaze3d/platform/Window;D)D", at = @At("RETURN"), cancellable = true)
    private static void ebsl$remapDockedScaledX(Window window, double rawX, CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue(EbslMod.postClientEvent(
            new ScaledMousePosEvent(window, rawX, cir.getReturnValueD(), ScaledMousePosEvent.Axis.X)).getScaledPos());
    }

    @Inject(method = "getScaledYPos(Lcom/mojang/blaze3d/platform/Window;D)D", at = @At("RETURN"), cancellable = true)
    private static void ebsl$remapDockedScaledY(Window window, double rawY, CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue(EbslMod.postClientEvent(
            new ScaledMousePosEvent(window, rawY, cir.getReturnValueD(), ScaledMousePosEvent.Axis.Y)).getScaledPos());
    }
}
