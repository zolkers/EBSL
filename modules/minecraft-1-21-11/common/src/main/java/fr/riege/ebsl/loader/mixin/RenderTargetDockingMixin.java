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
