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

import fr.riege.ebsl.tools.pathfindersim.replay.SimulationTick;

import java.awt.Font;
import java.awt.Graphics2D;
import java.util.Locale;

public final class ReplayHudRenderer {
    public void render(Graphics2D g, ReplayRenderContext context) {
        SimulationTick tick = context.result().ticksTrace().get(context.frame());
        g.setColor(ReplayPalette.TEXT);
        g.setFont(g.getFont().deriveFont(Font.PLAIN, 14f));
        g.drawString("scenario: " + context.result().scenarioId(), 24, 28);
        g.drawString("tick: " + tick.tick() + " move: " + tick.moveType()
            + " dist: " + format(tick.distanceToGoal()), 24, 48);
        g.drawString("pos: " + format(tick.position().x()) + ", "
            + format(tick.position().y()) + ", " + format(tick.position().z()), 24, 68);
    }

    private static String format(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }
}
