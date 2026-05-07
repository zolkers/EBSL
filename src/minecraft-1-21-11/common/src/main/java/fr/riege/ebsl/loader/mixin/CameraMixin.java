package fr.riege.ebsl.loader.mixin;

import fr.riege.ebsl.loader.ModloaderCommonBootstrap;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public final class CameraMixin {
    @Inject(method = "setup", at = @At("HEAD"))
    private void ebsl$beforeCameraSetup(Level level,
                                        Entity entity,
                                        boolean detached,
                                        boolean mirror,
                                        float partialTick,
                                        CallbackInfo ci) {
        ModloaderCommonBootstrap.beforeRenderWorld();
    }
}
