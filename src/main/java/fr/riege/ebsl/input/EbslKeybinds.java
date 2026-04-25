package fr.riege.ebsl.input;

import com.mojang.blaze3d.platform.InputConstants;
import fr.riege.ebsl.EbslMod;
import fr.riege.ebsl.ui.imgui.EbslImGuiOverlay;
import fr.riege.ebsl.ui.viewport.DockedMouseLock;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class EbslKeybinds {
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
        Identifier.fromNamespaceAndPath(EbslMod.MOD_ID, "main"));

    private static KeyMapping unfocusMinecraft;

    private EbslKeybinds() {
    }

    public static void register() {
        unfocusMinecraft = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key." + EbslMod.MOD_ID + ".unfocus_minecraft",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_ALT,
            CATEGORY));

        ClientTickEvents.END_CLIENT_TICK.register(EbslKeybinds::tick);
    }

    private static void tick(Minecraft client) {
        while (unfocusMinecraft.consumeClick()) {
            client.mouseHandler.releaseMouse();
        }
        DockedMouseLock.confineIfFocused(client);
        if (EbslImGuiOverlay.isVisible() && !client.mouseHandler.isMouseGrabbed()) {
            releaseGameplayKeys(client);
        }
    }

    private static void releaseGameplayKeys(Minecraft client) {
        client.options.keyUp.setDown(false);
        client.options.keyDown.setDown(false);
        client.options.keyLeft.setDown(false);
        client.options.keyRight.setDown(false);
        client.options.keyJump.setDown(false);
        client.options.keyShift.setDown(false);
        client.options.keySprint.setDown(false);
        client.options.keyAttack.setDown(false);
        client.options.keyUse.setDown(false);
        client.options.keyPickItem.setDown(false);
    }
}
