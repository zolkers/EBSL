package fr.riege.ebsl.loader.mixin;

import fr.riege.ebsl.common.platform.service.EbslServices;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyMapping.class)
public class KeyMappingNavigationMixin {
    private KeyMappingNavigationMixin() {
    }

    @Inject(method = "releaseAll", at = @At("HEAD"), cancellable = true)
    private static void ebslPreventReleaseWhenNavigating(CallbackInfo ci) {
        if (EbslServices.isNavigationActive()) {
            ci.cancel();
        }
    }
}
