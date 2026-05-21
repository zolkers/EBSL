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

package fr.riege.ebsl.fabric;

import fr.riege.ebsl.common.EbslCore;
import fr.riege.ebsl.common.feature.ui.CommonImGuiOverlay;
import fr.riege.ebsl.common.feature.ui.layout.UiRect;
import fr.riege.ebsl.common.platform.EbslPlatform;
import fr.riege.ebsl.common.platform.service.NavigationService;
import fr.riege.ebsl.common.platform.service.UiService;
import fr.riege.ebsl.loader.ModloaderApplicationBridge;
import fr.riege.ebsl.loader.ModloaderCommonBootstrap;
import fr.riege.ebsl.loader.layer.MinecraftPhysicsLayer;
import fr.riege.ebsl.loader.ui.ModloaderViewportRect;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;

import java.nio.file.Path;

public class FabricEbslMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Minecraft client = Minecraft.getInstance();
        Path configDir = FabricLoader.getInstance().getConfigDir().resolve("ebsl");
        ModloaderCommonBootstrap.initialize(
            client,
            configDir,
            new MinecraftPhysicsLayer(client),
            new FabricCommandLayer(),
            new FabricImGuiLayer(client),
            new FabricInputLayer(client),
            new FabricApplicationBridge());

        ClientTickEvents.END_CLIENT_TICK.register(ignored -> ModloaderCommonBootstrap.tick());
    }

    private static final class FabricApplicationBridge implements ModloaderApplicationBridge {
        @Override
        public void bootstrap(EbslPlatform platform, NavigationService navigationService, UiService uiService) {
            new EbslCore(platform, navigationService, uiService);
        }

        @Override
        public ModloaderViewportRect gameViewportRect(int width, int height) {
            UiRect rect = CommonImGuiOverlay.gameViewportRect(width, height);
            return new ModloaderViewportRect(rect.x(), rect.y(), rect.width(), rect.height());
        }

        @Override
        public boolean acceptsMinecraftFocusAt(double x, double y, int width, int height,
                                               UiService ui) {
            return CommonImGuiOverlay.acceptsMinecraftFocusAt(x, y, width, height, ui);
        }

        @Override
        public boolean shouldConfineMinecraftMouse(UiService ui) {
            return CommonImGuiOverlay.shouldConfineMinecraftMouse(ui);
        }
    }
}
