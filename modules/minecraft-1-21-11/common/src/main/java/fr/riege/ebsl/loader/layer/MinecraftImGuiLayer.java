package fr.riege.ebsl.loader.layer;

import fr.riege.ebsl.common.platform.layer.IImGuiLayer;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinecraftImGuiLayer implements IImGuiLayer {
    private static final Logger LOGGER = LoggerFactory.getLogger("ebsl-imgui");

    private final Minecraft client;
    private Runnable drawPanels;
    private boolean renderFailureLogged;

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
            try {
                drawPanels.run();
                renderFailureLogged = false;
            } catch (Throwable throwable) {
                if (!renderFailureLogged) {
                    LOGGER.error("EBSL ImGui frame failed", throwable);
                    renderFailureLogged = true;
                }
            }
        }
    }

    @Override public int getViewportWidth() {
        return client.getWindow().getScreenWidth();
    }

    @Override public int getViewportHeight() {
        return client.getWindow().getScreenHeight();
    }
}
