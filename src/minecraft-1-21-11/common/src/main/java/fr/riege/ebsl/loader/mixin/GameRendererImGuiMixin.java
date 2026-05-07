package fr.riege.ebsl.loader.mixin;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererImGuiMixin {
    @Inject(method = "render", at = @At("RETURN"))
    private void ebsl$renderImGui(DeltaTracker deltaTracker, boolean tick, CallbackInfo ci) {
        // ImGui is drawn from RenderTargetDockingMixin after the docked viewport composition.
    }
}
