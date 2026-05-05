package fr.riege.ebsl.fabric.layer;

import fr.riege.ebsl.common.layer.IImGuiLayer;
import net.minecraft.client.Minecraft;

public class FabricImGuiLayer implements IImGuiLayer {
    private final Minecraft client;
    private Runnable drawPanels;

    public FabricImGuiLayer(Minecraft client) { this.client = client; }

    @Override
    public void registerFrame(Runnable drawPanels) {
        this.drawPanels = drawPanels;
        // TODO: register with fabric-gui-imgui frame loop
    }

    @Override public int getViewportWidth() { throw new UnsupportedOperationException("TODO"); }
    @Override public int getViewportHeight() { throw new UnsupportedOperationException("TODO"); }
}
