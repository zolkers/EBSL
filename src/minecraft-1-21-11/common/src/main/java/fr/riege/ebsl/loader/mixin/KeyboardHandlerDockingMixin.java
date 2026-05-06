package fr.riege.ebsl.loader.mixin;

import fr.riege.ebsl.loader.ModloaderCommonBootstrap;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerDockingMixin {
    @Inject(method = "keyPress", at = @At("HEAD"))
    private void ebsl$onKeyPress(long window, int key, KeyEvent event, CallbackInfo ci) {
        ModloaderCommonBootstrap.onKeyPress(key, 1, 0);
    }

    @Inject(method = "charTyped", at = @At("HEAD"))
    private void ebsl$onCharTyped(long l, CharacterEvent characterEvent, CallbackInfo ci) {
        ModloaderCommonBootstrap.onCharTyped((char) characterEvent.codepoint());
    }
}
