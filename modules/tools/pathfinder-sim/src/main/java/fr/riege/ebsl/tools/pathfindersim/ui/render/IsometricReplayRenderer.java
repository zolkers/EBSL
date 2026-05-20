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
import fr.riege.ebsl.tools.pathfindersim.replay.ReplayBlock;
import fr.riege.ebsl.tools.pathfindersim.replay.SimulationTick;
import fr.riege.ebsl.tools.pathfindersim.ui.Bounds;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.util.List;

public final class IsometricReplayRenderer implements ReplayRenderer {
    @Override
    public void render(Graphics2D g, ReplayRenderContext context) {
        Bounds bounds = context.model().bounds();
        paintTerrain(g, context, bounds);
        paintPath(g, context, bounds);
        paintPlayer(g, context, bounds);
    }

    private static void paintTerrain(Graphics2D g, ReplayRenderContext context, Bounds bounds) {
        for (ReplayBlock block : context.model().isoTerrain()) {
            paintBlock(g, context, bounds, block);
        }
    }

    private static void paintBlock(Graphics2D g, ReplayRenderContext context, Bounds bounds, ReplayBlock block) {
        Color base = ReplayPalette.blockColor(block, context.model().terrainMinY());
        Path2D.Double south = face(bounds, context.camera(), context.model().terrainMinY(), block, 0.0, Face.SOUTH);
        Path2D.Double east = face(bounds, context.camera(), context.model().terrainMinY(), block, 0.0, Face.EAST);
        Path2D.Double top = face(bounds, context.camera(), context.model().terrainMinY(), block, 1.0, Face.TOP);
        fillFace(g, south, ReplayPalette.shade(base, -28));
        fillFace(g, east, ReplayPalette.shade(base, -50));
        fillFace(g, top, base);
    }

    private static Path2D.Double face(
        Bounds bounds,
        ReplayCamera camera,
        int terrainMinY,
        ReplayBlock block,
        double yOffset,
        Face face
    ) {
        return switch (face) {
            case TOP -> polygon(
                point(bounds, camera, terrainMinY, block.x(), block.y() + yOffset, block.z()),
                point(bounds, camera, terrainMinY, block.x() + 1.0, block.y() + yOffset, block.z()),
                point(bounds, camera, terrainMinY, block.x() + 1.0, block.y() + yOffset, block.z() + 1.0),
                point(bounds, camera, terrainMinY, block.x(), block.y() + yOffset, block.z() + 1.0));
            case SOUTH -> polygon(
                point(bounds, camera, terrainMinY, block.x(), block.y() + 1.0, block.z() + 1.0),
                point(bounds, camera, terrainMinY, block.x() + 1.0, block.y() + 1.0, block.z() + 1.0),
                point(bounds, camera, terrainMinY, block.x() + 1.0, block.y(), block.z() + 1.0),
                point(bounds, camera, terrainMinY, block.x(), block.y(), block.z() + 1.0));
            case EAST -> polygon(
                point(bounds, camera, terrainMinY, block.x() + 1.0, block.y() + 1.0, block.z()),
                point(bounds, camera, terrainMinY, block.x() + 1.0, block.y() + 1.0, block.z() + 1.0),
                point(bounds, camera, terrainMinY, block.x() + 1.0, block.y(), block.z() + 1.0),
                point(bounds, camera, terrainMinY, block.x() + 1.0, block.y(), block.z()));
        };
    }

    private static double[] point(Bounds bounds, ReplayCamera camera, int terrainMinY, double x, double y, double z) {
        return ReplayProjection.isoPoint(bounds, camera, terrainMinY, x, y, z);
    }

    private static Path2D.Double polygon(double[] first, double[] second, double[] third, double[] fourth) {
        Path2D.Double path = new Path2D.Double();
        path.moveTo(first[0], first[1]);
        path.lineTo(second[0], second[1]);
        path.lineTo(third[0], third[1]);
        path.lineTo(fourth[0], fourth[1]);
        path.closePath();
        return path;
    }

    private static void fillFace(Graphics2D g, Path2D.Double face, Color color) {
        g.setColor(color);
        g.fill(face);
        g.draw(face);
    }

    private static void paintPath(Graphics2D g, ReplayRenderContext context, Bounds bounds) {
        List<SimulationTick> ticks = context.result().ticksTrace();
        g.setStroke(new BasicStroke(2.0f));
        for (int index = 1; index <= context.frame(); index++) {
            SimulationTick previous = ticks.get(index - 1);
            SimulationTick current = ticks.get(index);
            g.setColor(current.stuck() ? ReplayPalette.STUCK : ReplayPalette.PATH);
            drawLine(g, context, bounds, previous.position(), current.position());
        }
    }

    private static void drawLine(Graphics2D g, ReplayRenderContext context, Bounds bounds, Vec3d from, Vec3d to) {
        double[] start = point(bounds, context.camera(), context.model().terrainMinY(), from.x(), from.y(), from.z());
        double[] end = point(bounds, context.camera(), context.model().terrainMinY(), to.x(), to.y(), to.z());
        g.draw(new Line2D.Double(start[0], start[1], end[0], end[1]));
    }

    private static void paintPlayer(Graphics2D g, ReplayRenderContext context, Bounds bounds) {
        SimulationTick tick = context.result().ticksTrace().get(context.frame());
        double[] point = point(
            bounds,
            context.camera(),
            context.model().terrainMinY(),
            tick.position().x(),
            tick.position().y(),
            tick.position().z());
        double x = point[0];
        double y = point[1];
        g.setColor(tick.stuck() ? ReplayPalette.STUCK : ReplayPalette.PLAYER);
        g.fill(new Ellipse2D.Double(x - 7.0, y - 16.0, 14.0, 14.0));
        g.setColor(ReplayPalette.TEXT);
        g.draw(new Line2D.Double(x, y - 2.0, x, y - 15.0));
        g.draw(new Ellipse2D.Double(x - 10.0, y - 19.0, 20.0, 20.0));
    }

    private enum Face {
        TOP,
        SOUTH,
        EAST
    }
}
