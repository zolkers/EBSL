package fr.riege.ebsl.fabric;

import com.mojang.blaze3d.platform.InputConstants;
import fr.riege.ebsl.loader.layer.MinecraftInputLayer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class FabricInputLayer extends MinecraftInputLayer {
    private static final String MOD_ID = "ebsl";

    public FabricInputLayer(Minecraft client) {
        super(client);
    }

    @Override
    public void registerUnfocusKeybind(Runnable action) {
        KeyMapping.Category category = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(MOD_ID, "main"));
        KeyMapping unfocusMinecraft = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key." + MOD_ID + ".unfocus_minecraft",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_ALT,
            category));

        ClientTickEvents.END_CLIENT_TICK.register(ignored -> {
            while (unfocusMinecraft.consumeClick()) {
                action.run();
            }
        });
    }
}
