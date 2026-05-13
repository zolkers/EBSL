/*
 * This file is part of EBSL.
 *
 * EBSL is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EBSL is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

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
            } catch (Exception exception) {
                if (!renderFailureLogged) {
                    LOGGER.error("EBSL ImGui frame failed", exception);
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
