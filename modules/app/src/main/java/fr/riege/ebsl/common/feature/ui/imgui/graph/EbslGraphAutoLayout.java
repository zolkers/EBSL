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

package fr.riege.ebsl.common.feature.ui.imgui.graph;

import fr.riege.ebsl.common.feature.scripting.manager.EbslGraphNodePosition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class EbslGraphAutoLayout {
    private static final float START_X = 28.0f;
    private static final float START_Y = 28.0f;
    private static final float COLUMN_WIDTH = 248.0f;
    private static final float ROW_HEIGHT = 92.0f;
    private static final float BLOCK_EXTRA_GAP = 18.0f;

    private EbslGraphAutoLayout() {
    }

    public static Map<String, EbslGraphNodePosition> layout(List<EbslScriptGraphNode> nodes) {
        Map<String, EbslGraphNodePosition> positions = new HashMap<>();
        float y = START_Y;
        int previousDepth = 0;
        for (EbslScriptGraphNode node : nodes) {
            if (!positions.isEmpty() && node.depth() < previousDepth) {
                y += BLOCK_EXTRA_GAP * (previousDepth - node.depth());
            }
            float x = START_X + node.depth() * COLUMN_WIDTH;
            positions.put(node.key(), new EbslGraphNodePosition(x, y));
            y += ROW_HEIGHT + (node.blockStart() ? BLOCK_EXTRA_GAP : 0.0f);
            previousDepth = node.depth();
        }
        return positions;
    }
}
