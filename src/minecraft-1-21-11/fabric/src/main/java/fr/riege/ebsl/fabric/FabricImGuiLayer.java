package fr.riege.ebsl.fabric;

import cn.enaium.fabric.imgui.FabricImGui;
import fr.riege.ebsl.loader.layer.MinecraftImGuiLayer;
import net.minecraft.client.Minecraft;

public final class FabricImGuiLayer extends MinecraftImGuiLayer {
    public FabricImGuiLayer(Minecraft client) {
        super(client);
    }

    @Override
    public void drawFrame() {
        FabricImGui.IMGUI.draw(io -> drawRegisteredFrame());
    }
}
