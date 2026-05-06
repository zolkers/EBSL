package fr.riege.ebsl.loader.mixin;

import fr.riege.ebsl.loader.ModloaderCommonBootstrap;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerDockingMixin {
    @Inject(method = "onButton", at = @At("HEAD"))
    private void ebsl$onMouseButton(long windowHandle, MouseButtonInfo button, int action, CallbackInfo ci) {
        ModloaderCommonBootstrap.onMouseButton(button.button(), action);
    }
}
