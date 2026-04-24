package fr.riege.ebsl.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import fr.riege.ebsl.ui.imgui.EbslImGuiOverlay;
import fr.riege.ebsl.ui.viewport.DockedMinecraftCompositor;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderTarget.class)
public final class RenderTargetDockingMixin {
    @Inject(method = "blitToScreen", at = @At("HEAD"))
    private void ebsl$composeDockedViewportBeforePresent(CallbackInfo ci) {
        RenderTarget target = (RenderTarget) (Object) this;
        if (target != Minecraft.getInstance().getMainRenderTarget() || !EbslImGuiOverlay.isVisible()) {
            return;
        }

        DockedMinecraftCompositor.compose(target);
        EbslImGuiOverlay.render();
    }
}
