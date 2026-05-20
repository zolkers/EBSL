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

package fr.riege.ebsl.tools.pathfindersim.ui.render;

import fr.riege.ebsl.tools.pathfindersim.replay.ReplayBlock;

import java.awt.Color;

public final class ReplayPalette {
    public static final Color BACKGROUND = new Color(18, 22, 27);
    public static final Color GRID = new Color(48, 56, 65);
    public static final Color PATH = new Color(80, 170, 255);
    public static final Color STUCK = new Color(255, 96, 96);
    public static final Color PLAYER = new Color(115, 230, 145);
    public static final Color TEXT = new Color(226, 232, 240);

    private ReplayPalette() {
    }

    public static Color blockColor(ReplayBlock block, int terrainMinY) {
        Color base = new Color(block.kind().baseRgb());
        long heightDelta = (long) block.y() - (long) terrainMinY;
        int elevation = Math.toIntExact(Math.clamp(heightDelta * 5L, -36L, 52L));
        return shade(base, elevation);
    }

    public static Color columnColor(TerrainColumn column, int terrainMinY) {
        Color base = new Color(column.kind().baseRgb());
        int heightShade = Math.toIntExact(Math.clamp(((long) column.y() - terrainMinY) * 4L, -34L, 44L));
        int reliefShade = Math.toIntExact(Math.clamp(column.relief() * 18L, -42L, 42L));
        return shade(base, heightShade + reliefShade);
    }

    public static Color shade(Color color, int delta) {
        return new Color(
            colorChannel(color.getRed(), delta),
            colorChannel(color.getGreen(), delta),
            colorChannel(color.getBlue(), delta));
    }

    private static int colorChannel(int channel, int delta) {
        long adjusted = (long) channel + (long) delta;
        return Math.toIntExact(Math.clamp(adjusted, 0L, 255L));
    }
}
