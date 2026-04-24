package fr.riege.ebsl.mixin;

import com.mojang.blaze3d.platform.Window;
import fr.riege.ebsl.ui.imgui.EbslImGuiOverlay;
import fr.riege.ebsl.ui.viewport.DockedMouseCoordinates;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerDockingMixin {
    @Shadow
    public abstract double xpos();

    @Shadow
    public abstract double ypos();

    @Inject(method = "grabMouse", at = @At("HEAD"), cancellable = true)
    private void ebsl$onlyGrabMouseFromDockedViewport(CallbackInfo ci) {
        if (!EbslImGuiOverlay.isVisible()) {
            return;
        }

        Window window = Minecraft.getInstance().getWindow();
        if (!EbslImGuiOverlay.acceptsMinecraftFocusAt(
            xpos(),
            ypos(),
            window.getScreenWidth(),
            window.getScreenHeight())) {
            ci.cancel();
        }
    }

    @Inject(method = "onButton", at = @At("HEAD"), cancellable = true)
    private void ebsl$blockMinecraftClicksOutsideDockedViewport(long windowHandle, MouseButtonInfo button, int action,
                                                                CallbackInfo ci) {
        if (!EbslImGuiOverlay.isVisible()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Window window = minecraft.getWindow();
        if (!EbslImGuiOverlay.acceptsMinecraftFocusAt(
            xpos(),
            ypos(),
            window.getScreenWidth(),
            window.getScreenHeight())) {
            minecraft.mouseHandler.releaseMouse();
            ci.cancel();
        }
    }

    @Inject(method = "getScaledXPos(Lcom/mojang/blaze3d/platform/Window;D)D", at = @At("RETURN"), cancellable = true)
    private static void ebsl$remapDockedScaledX(Window window, double rawX, CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue(DockedMouseCoordinates.remapScaledX(window, rawX, cir.getReturnValueD()));
    }

    @Inject(method = "getScaledYPos(Lcom/mojang/blaze3d/platform/Window;D)D", at = @At("RETURN"), cancellable = true)
    private static void ebsl$remapDockedScaledY(Window window, double rawY, CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue(DockedMouseCoordinates.remapScaledY(window, rawY, cir.getReturnValueD()));
    }
}
