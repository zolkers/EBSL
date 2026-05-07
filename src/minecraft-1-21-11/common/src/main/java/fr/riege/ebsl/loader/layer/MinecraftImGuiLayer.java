package fr.riege.ebsl.loader.layer;

import fr.riege.ebsl.common.platform.layer.IImGuiLayer;
import net.minecraft.client.Minecraft;

public class MinecraftImGuiLayer implements IImGuiLayer {
    private final Minecraft client;
    private Runnable drawPanels;

    public MinecraftImGuiLayer(Minecraft client) {
        this.client = client;
    }

    @Override public void registerFrame(Runnable drawPanels) {
        this.drawPanels = drawPanels;
    }

    public void drawFrame() {
        drawRegisteredFrame();
    }

    protected final void drawRegisteredFrame() {
        if (drawPanels != null) {
            drawPanels.run();
        }
    }

    @Override public int getViewportWidth() {
        return client.getWindow().getScreenWidth();
    }

    @Override public int getViewportHeight() {
        return client.getWindow().getScreenHeight();
    }
}
