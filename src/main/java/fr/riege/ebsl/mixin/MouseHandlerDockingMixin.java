package fr.riege.ebsl.mixin;

import com.mojang.blaze3d.platform.Window;
import fr.riege.ebsl.ui.viewport.DockedMouseCoordinates;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerDockingMixin {
    @Inject(method = "getScaledXPos(Lcom/mojang/blaze3d/platform/Window;D)D", at = @At("RETURN"), cancellable = true)
    private static void ebsl$remapDockedScaledX(Window window, double rawX, CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue(DockedMouseCoordinates.remapScaledX(window, rawX, cir.getReturnValueD()));
    }

    @Inject(method = "getScaledYPos(Lcom/mojang/blaze3d/platform/Window;D)D", at = @At("RETURN"), cancellable = true)
    private static void ebsl$remapDockedScaledY(Window window, double rawY, CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue(DockedMouseCoordinates.remapScaledY(window, rawY, cir.getReturnValueD()));
    }
}
