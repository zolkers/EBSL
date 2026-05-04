package fr.riege.ebsl.mixin;

import fr.riege.ebsl.EbslMod;
import fr.riege.ebsl.event.events.input.CharTypedEvent;
import fr.riege.ebsl.event.events.input.KeyPressEvent;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerDockingMixin {
    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    private void ebsl$onKeyPress(long window, int key, KeyEvent event, CallbackInfo ci) {
        if (EbslMod.postClientEvent(new KeyPressEvent(window, key, event)).isCancelled()) ci.cancel();
    }

    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
    private void ebsl$onCharTyped(long l, CharacterEvent characterEvent, CallbackInfo ci) {
        if (EbslMod.postClientEvent(new CharTypedEvent(l, characterEvent)).isCancelled()) ci.cancel();
    }
}
