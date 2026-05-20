/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Copyright (C) 2026 Victor Riegert and EBSL contributors
 *
 * This file is part of EBSL.
 *
 * EBSL is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EBSL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EBSL. If not, see <https://www.gnu.org/licenses/>.
 */

package fr.riege.ebsl.loader.ui;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Window;
import fr.riege.ebsl.common.core.event.CharTypedEvent;
import fr.riege.ebsl.common.core.event.KeyPressEvent;
import fr.riege.ebsl.common.core.event.MouseButtonEvent;
import fr.riege.ebsl.common.core.event.ScaledMousePosEvent;
import fr.riege.ebsl.common.core.event.events.input.GrabMouseEvent;
import fr.riege.ebsl.common.feature.ui.CommonImGuiOverlay;
import fr.riege.ebsl.loader.layer.MinecraftImGuiLayer;
import fr.riege.ebsl.loader.layer.ModloaderEventBus;
import fr.riege.ebsl.loader.layer.ModloaderUiService;
import fr.riege.ebsl.loader.viewport.DockedMinecraftCompositor;
import fr.riege.ebsl.loader.viewport.DockedMouseCoordinates;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public final class DockingInputHandler {
    private final Minecraft client;
    private final ModloaderUiService ui;
    private final MinecraftImGuiLayer imgui;

    private DockingInputHandler(Minecraft client, ModloaderUiService ui, MinecraftImGuiLayer imgui) {
        this.client = client;
        this.ui = ui;
        this.imgui = imgui;
    }

    public static DockingInputHandler register(ModloaderEventBus bus,
                                               Minecraft client,
                                               ModloaderUiService ui,
                                               MinecraftImGuiLayer imgui) {
        DockingInputHandler handler = new DockingInputHandler(client, ui, imgui);
        bus.subscribe(GrabMouseEvent.class, handler::onGrabMouse);
        bus.subscribe(MouseButtonEvent.class, handler::onMouseButton);
        bus.subscribe(KeyPressEvent.class, handler::onKeyPress);
        bus.subscribe(CharTypedEvent.class, handler::onCharTyped);
        bus.subscribe(ScaledMousePosEvent.class, handler::onScaledMousePos);
        return handler;
    }

    public void onBlitToScreen(RenderTarget target) {
        if (target != client.getMainRenderTarget() || !ui.isVisible()) {
            return;
        }
        DockedMinecraftCompositor.compose(target);
        imgui.drawFrame();
    }

    public boolean shouldSuppressImGuiInput() {
        return CommonImGuiOverlay.shouldConfineMinecraftMouse(ui) && client.mouseHandler.isMouseGrabbed();
    }

    private void onGrabMouse(GrabMouseEvent event) {
        if (ui.isVisible() && !CommonImGuiOverlay.shouldConfineMinecraftMouse(ui)) {
            event.cancel();
        }
    }

    private void onMouseButton(MouseButtonEvent event) {
        if (!ui.isVisible()) return;
        Window window = client.getWindow();
        if (event.windowHandle() != window.handle()) return;
        double[] x = {0.0};
        double[] y = {0.0};
        GLFW.glfwGetCursorPos(event.windowHandle(), x, y);

        if (client.screen != null) {
            if (!CommonImGuiOverlay.acceptsMinecraftFocusAt(
                x[0], y[0], window.getScreenWidth(), window.getScreenHeight(), ui)) {
                event.cancel();
            } else if (client.mouseHandler.isMouseGrabbed()) {
                client.mouseHandler.releaseMouse();
            }
            return;
        }

        if (client.mouseHandler.isMouseGrabbed()) return;
        if (!CommonImGuiOverlay.acceptsMinecraftFocusAt(
            x[0], y[0], window.getScreenWidth(), window.getScreenHeight(), ui)) {
            client.mouseHandler.releaseMouse();
            event.cancel();
        } else {
            client.mouseHandler.grabMouse();
        }
    }

    private void onKeyPress(KeyPressEvent event) {
        if (shouldRouteKeyboardToImGui()) {
            event.cancel();
        }
    }

    private void onCharTyped(CharTypedEvent event) {
        if (shouldRouteKeyboardToImGui()) {
            event.cancel();
        }
    }

    private void onScaledMousePos(ScaledMousePosEvent event) {
        Window window = client.getWindow();
        if (event.axis() == ScaledMousePosEvent.Axis.X) {
            event.setScaledPos(DockedMouseCoordinates.remapScaledX(window, event.rawPos(), event.scaledPos(), ui));
        } else {
            event.setScaledPos(DockedMouseCoordinates.remapScaledY(window, event.rawPos(), event.scaledPos(), ui));
        }
    }

    private boolean shouldRouteKeyboardToImGui() {
        return ui.isVisible() && client.screen == null && !client.mouseHandler.isMouseGrabbed();
    }
}
