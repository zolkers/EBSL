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

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.tools.pathfindersim.replay.SimulationTick;
import fr.riege.ebsl.tools.pathfindersim.ui.Bounds;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

public final class TopDownReplayRenderer implements ReplayRenderer {
    @Override
    public void render(Graphics2D g, ReplayRenderContext context) {
        Bounds bounds = context.model().bounds();
        paintTerrain(g, context, bounds);
        paintPath(g, context, bounds);
        paintPlayer(g, context, bounds);
    }

    private static void paintTerrain(Graphics2D g, ReplayRenderContext context, Bounds bounds) {
        for (TerrainColumn column : context.model().surfaceTerrain()) {
            g.setColor(ReplayPalette.columnColor(column, context.model().terrainMinY()));
            double left = ReplayProjection.screenX(bounds, context.camera(), column.x());
            double right = ReplayProjection.screenX(bounds, context.camera(), column.x() + 1.0);
            double top = ReplayProjection.screenY(bounds, context.camera(), column.z() + 1.0);
            double bottom = ReplayProjection.screenY(bounds, context.camera(), column.z());
            double x = Math.floor(Math.min(left, right));
            double y = Math.floor(Math.min(top, bottom));
            double width = Math.ceil(Math.max(left, right)) - x + 1.0;
            double height = Math.ceil(Math.max(top, bottom)) - y + 1.0;
            g.fill(new Rectangle2D.Double(x, y, width, height));
        }
    }

    private static void paintPath(Graphics2D g, ReplayRenderContext context, Bounds bounds) {
        List<SimulationTick> ticks = context.result().ticksTrace();
        g.setStroke(new BasicStroke(2.0f));
        for (int index = 1; index <= context.frame(); index++) {
            SimulationTick previous = ticks.get(index - 1);
            SimulationTick current = ticks.get(index);
            g.setColor(current.stuck() ? ReplayPalette.STUCK : ReplayPalette.PATH);
            drawLine(g, bounds, context.camera(), previous.position(), current.position());
        }
    }

    private static void paintPlayer(Graphics2D g, ReplayRenderContext context, Bounds bounds) {
        SimulationTick tick = context.result().ticksTrace().get(context.frame());
        double x = ReplayProjection.screenX(bounds, context.camera(), tick.position().x());
        double y = ReplayProjection.screenY(bounds, context.camera(), tick.position().z());
        g.setColor(tick.stuck() ? ReplayPalette.STUCK : ReplayPalette.PLAYER);
        g.fill(new Ellipse2D.Double(x - 6.0, y - 6.0, 12.0, 12.0));
        g.setColor(ReplayPalette.TEXT);
        g.draw(new Ellipse2D.Double(x - 9.0, y - 9.0, 18.0, 18.0));
    }

    private static void drawLine(Graphics2D g, Bounds bounds, ReplayCamera camera, Vec3d from, Vec3d to) {
        double startX = ReplayProjection.screenX(bounds, camera, from.x());
        double startY = ReplayProjection.screenY(bounds, camera, from.z());
        double endX = ReplayProjection.screenX(bounds, camera, to.x());
        double endY = ReplayProjection.screenY(bounds, camera, to.z());
        g.draw(new Line2D.Double(startX, startY, endX, endY));
    }
}
