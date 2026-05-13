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

package fr.riege.ebsl.mc;

import fr.riege.ebsl.common.platform.layer.IRenderLayer;
import fr.riege.ebsl.common.platform.layer.IStorageLayer;
import fr.riege.ebsl.common.world.layer.IEntityLayer;
import fr.riege.ebsl.common.world.layer.IPlayerLayer;
import fr.riege.ebsl.common.world.layer.IWorldLayer;
import net.minecraft.client.Minecraft;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Groups Minecraft-backed platform layer contracts.
 */
public record McPlatformLayers(
    IWorldLayer world,
    IPlayerLayer player,
    IRenderLayer render,
    IStorageLayer storage,
    IEntityLayer entities
) {
    /**
     * Creates the platform layer set for a Minecraft client.
     *
     * @param client the Minecraft client
     * @param configDir the configuration directory used by storage
     * @return Minecraft-backed platform layers exposed through contracts
     */
    public static McPlatformLayers create(Minecraft client, Path configDir) {
        Minecraft effectiveClient = Objects.requireNonNull(client, "client");
        return new McPlatformLayers(
            new McWorldLayer(effectiveClient),
            new McPlayerLayer(effectiveClient),
            new McRenderLayer(),
            new McStorageLayer(Objects.requireNonNull(configDir, "configDir")),
            new McEntityLayer(effectiveClient));
    }
}
