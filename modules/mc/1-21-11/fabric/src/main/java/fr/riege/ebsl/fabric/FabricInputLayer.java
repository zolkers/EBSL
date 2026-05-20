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
