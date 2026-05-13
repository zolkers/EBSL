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
package fr.riege.ebsl.fabric;

import fr.riege.ebsl.loader.ModloaderCommonBootstrap;
import fr.riege.ebsl.loader.layer.MinecraftPhysicsLayer;
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
            new FabricInputLayer(client));

        ClientTickEvents.END_CLIENT_TICK.register(ignored -> ModloaderCommonBootstrap.tick());
    }
}
