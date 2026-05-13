package fr.riege.ebsl.loader.mixin;

import fr.riege.ebsl.loader.ModloaderCommonBootstrap;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiHudMixin {
    @Inject(method = "render", at = @At("RETURN"))
    private void ebslOnRenderHud(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        ModloaderCommonBootstrap.onRenderHud(
            client.getWindow().getGuiScaledWidth(),
            client.getWindow().getGuiScaledHeight(),
            deltaTracker.getGameTimeDeltaTicks());
    }
}
