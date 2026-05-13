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

package fr.riege.ebsl.loader.render;

import net.minecraft.client.renderer.DynamicUniformStorage;
import org.joml.Matrix4f;
import org.jspecify.annotations.NonNull;

import java.nio.ByteBuffer;

public final class EbslMeshUniforms implements DynamicUniformStorage.DynamicUniform {
    static final int BLOCK_SIZE = 256;

    final Matrix4f projection = new Matrix4f();
    final Matrix4f modelView = new Matrix4f();
    float screenWidth = 1.0f;
    float screenHeight = 1.0f;

    @Override
    public void write(@NonNull ByteBuffer buffer) {
        projection.get(0, buffer);
        modelView.get(64, buffer);
        buffer.putFloat(128, screenWidth);
        buffer.putFloat(132, screenHeight);
    }
}
