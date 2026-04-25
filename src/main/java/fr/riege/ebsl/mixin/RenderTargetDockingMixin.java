package fr.riege.ebsl.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import fr.riege.ebsl.EbslMod;
import fr.riege.ebsl.event.events.render.BlitToScreenEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderTarget.class)
public final class RenderTargetDockingMixin {
    @Inject(method = "blitToScreen", at = @At("HEAD"))
    private void ebsl$onBlitToScreen(CallbackInfo ci) {
        EbslMod.postClientEvent(new BlitToScreenEvent((RenderTarget) (Object) this));
    }
}
