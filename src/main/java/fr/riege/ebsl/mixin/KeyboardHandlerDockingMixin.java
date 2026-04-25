package fr.riege.ebsl.mixin;

import fr.riege.ebsl.ui.imgui.EbslImGuiOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerDockingMixin {
    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    private void ebsl$blockMinecraftKeysWhenDockUnfocused(long window, int key, KeyEvent event, CallbackInfo ci) {
        if (shouldRouteKeyboardToImGui()) {
            ci.cancel();
        }
    }

    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
    private void ebsl$blockMinecraftTextWhenDockUnfocused(long window, int codePoint, int modifiers,
                                                          CallbackInfo ci) {
        if (shouldRouteKeyboardToImGui()) {
            ci.cancel();
        }
    }

    private static boolean shouldRouteKeyboardToImGui() {
        Minecraft minecraft = Minecraft.getInstance();
        return EbslImGuiOverlay.isVisible()
            && minecraft.mouseHandler != null
            && !minecraft.mouseHandler.isMouseGrabbed();
    }
}
