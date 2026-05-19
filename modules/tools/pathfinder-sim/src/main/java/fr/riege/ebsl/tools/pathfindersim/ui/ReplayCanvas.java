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

package fr.riege.ebsl.tools.pathfindersim.ui;

import fr.riege.ebsl.common.math.Vec3d;
import fr.riege.ebsl.tools.pathfindersim.replay.ReplayBlock;
import fr.riege.ebsl.tools.pathfindersim.replay.SimulationResult;
import fr.riege.ebsl.tools.pathfindersim.replay.SimulationTick;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.List;
import java.util.Locale;

final class ReplayCanvas extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final Color BACKGROUND = new Color(18, 22, 27);
    private static final Color GRID = new Color(48, 56, 65);
    private static final Color SOLID = new Color(91, 101, 112);
    private static final Color GRASS = new Color(82, 132, 67);
    private static final Color LEAVES = new Color(54, 112, 55);
    private static final Color EARTH = new Color(111, 84, 56);
    private static final Color SAND = new Color(194, 177, 113);
    private static final Color STONE = new Color(102, 108, 114);
    private static final Color WOOD = new Color(130, 91, 53);
    private static final Color SNOW = new Color(217, 228, 230);
    private static final Color WATER = new Color(43, 111, 184);
    private static final Color CLIMBABLE = new Color(186, 138, 72);
    private static final Color DANGER = new Color(180, 63, 55);
    private static final Color PATH = new Color(80, 170, 255);
    private static final Color STUCK = new Color(255, 96, 96);
    private static final Color PLAYER = new Color(115, 230, 145);
    private static final Color TEXT = new Color(226, 232, 240);
    private static final double MIN_ZOOM = 0.35;
    private static final double MAX_ZOOM = 6.0;
    private static final double WHEEL_ZOOM_FACTOR = 1.12;

    private transient SimulationResult result;
    private transient Bounds replayBounds;
    private transient List<ReplayBlock> isoTerrain = List.of();
    private transient Point lastDrag;
    private int frame;
    private int terrainMinY;
    private boolean isometric = true;
    private double viewZoom = 1.0;
    private double panX;
    private double panY;

    ReplayCanvas() {
        setBackground(BACKGROUND);
        CameraMouseHandler camera = new CameraMouseHandler();
        addMouseListener(camera);
        addMouseMotionListener(camera);
        addMouseWheelListener(camera);
    }

    void setResult(SimulationResult result) {
        this.result = result;
        this.replayBounds = boundsFor(result);
        this.isoTerrain = sortedIsoTerrain(result);
        this.terrainMinY = minTerrainY(result);
        this.frame = 0;
        resetCamera();
        repaint();
    }

    void setFrame(int frame) {
        this.frame = Math.max(0, frame);
        repaint();
    }

    void setIsometric(boolean isometric) {
        this.isometric = isometric;
        repaint();
    }

    void resetCamera() {
        viewZoom = 1.0;
        panX = 0.0;
        panY = 0.0;
        lastDrag = null;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        paintGrid(g);
        if (result == null || result.ticksTrace().isEmpty()) {
            paintEmpty(g);
        } else {
            paintReplay(g);
        }
        g.dispose();
    }

    private void paintGrid(Graphics2D g) {
        g.setColor(GRID);
        int spacing = 32;
        for (int x = 0; x < getWidth(); x += spacing) {
            g.drawLine(x, 0, x, getHeight());
        }
        for (int y = 0; y < getHeight(); y += spacing) {
            g.drawLine(0, y, getWidth(), y);
        }
    }

    private void paintEmpty(Graphics2D g) {
        g.setColor(TEXT);
        g.setFont(g.getFont().deriveFont(Font.BOLD, 18f));
        g.drawString("No replay loaded", 28, 42);
    }

    private void paintReplay(Graphics2D g) {
        List<SimulationTick> ticks = result.ticksTrace();
        Bounds bounds = replayBounds;
        int cappedFrame = Math.min(frame, ticks.size() - 1);
        if (isometric) {
            paintIsometricReplay(g, bounds, cappedFrame);
            return;
        }
        paintTerrain(g, bounds);
        g.setStroke(new BasicStroke(2.0f));
        for (int index = 1; index <= cappedFrame; index++) {
            SimulationTick previous = ticks.get(index - 1);
            SimulationTick current = ticks.get(index);
            g.setColor(current.stuck() ? STUCK : PATH);
            drawLine(g, bounds, previous.position(), current.position());
        }
        SimulationTick tick = ticks.get(cappedFrame);
        drawPlayer(g, bounds, tick);
        paintHud(g, tick);
    }

    private void paintIsometricReplay(Graphics2D g, Bounds bounds, int cappedFrame) {
        List<SimulationTick> ticks = result.ticksTrace();
        paintIsometricTerrain(g, bounds);
        g.setStroke(new BasicStroke(2.0f));
        for (int index = 1; index <= cappedFrame; index++) {
            SimulationTick previous = ticks.get(index - 1);
            SimulationTick current = ticks.get(index);
            g.setColor(current.stuck() ? STUCK : PATH);
            drawIsoLine(g, bounds, previous.position(), current.position());
        }
        SimulationTick tick = ticks.get(cappedFrame);
        drawIsoPlayer(g, bounds, tick);
        paintHud(g, tick);
    }

    private void paintTerrain(Graphics2D g, Bounds bounds) {
        for (ReplayBlock block : result.terrain()) {
            g.setColor(blockColor(block));
            double left = screenX(bounds, block.x());
            double right = screenX(bounds, block.x() + 1.0);
            double top = screenY(bounds, block.z() + 1.0);
            double bottom = screenY(bounds, block.z());
            double x = Math.floor(Math.min(left, right));
            double y = Math.floor(Math.min(top, bottom));
            double width = Math.ceil(Math.max(left, right)) - x + 1.0;
            double height = Math.ceil(Math.max(top, bottom)) - y + 1.0;
            g.fill(new Rectangle2D.Double(x, y, width, height));
        }
    }

    private void paintIsometricTerrain(Graphics2D g, Bounds bounds) {
        for (ReplayBlock block : isoTerrain) {
            paintIsoBlock(g, bounds, block);
        }
    }

    private void paintIsoBlock(Graphics2D g, Bounds bounds, ReplayBlock block) {
        Color base = blockColor(block);
        Path2D.Double top = isoFace(bounds, block, 1.0, Face.TOP);
        Path2D.Double south = isoFace(bounds, block, 0.0, Face.SOUTH);
        Path2D.Double east = isoFace(bounds, block, 0.0, Face.EAST);
        fillFace(g, south, shade(base, -28));
        fillFace(g, east, shade(base, -50));
        fillFace(g, top, base);
    }

    private Path2D.Double isoFace(Bounds bounds, ReplayBlock block, double yOffset, Face face) {
        return switch (face) {
            case TOP -> polygon(
                isoPoint(bounds, block.x(), block.y() + yOffset, block.z()),
                isoPoint(bounds, block.x() + 1.0, block.y() + yOffset, block.z()),
                isoPoint(bounds, block.x() + 1.0, block.y() + yOffset, block.z() + 1.0),
                isoPoint(bounds, block.x(), block.y() + yOffset, block.z() + 1.0));
            case SOUTH -> polygon(
                isoPoint(bounds, block.x(), block.y() + 1.0, block.z() + 1.0),
                isoPoint(bounds, block.x() + 1.0, block.y() + 1.0, block.z() + 1.0),
                isoPoint(bounds, block.x() + 1.0, block.y(), block.z() + 1.0),
                isoPoint(bounds, block.x(), block.y(), block.z() + 1.0));
            case EAST -> polygon(
                isoPoint(bounds, block.x() + 1.0, block.y() + 1.0, block.z()),
                isoPoint(bounds, block.x() + 1.0, block.y() + 1.0, block.z() + 1.0),
                isoPoint(bounds, block.x() + 1.0, block.y(), block.z() + 1.0),
                isoPoint(bounds, block.x() + 1.0, block.y(), block.z()));
        };
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

    private void paintHud(Graphics2D g, SimulationTick tick) {
        g.setColor(TEXT);
        g.setFont(g.getFont().deriveFont(Font.PLAIN, 14f));
        g.drawString("scenario: " + result.scenarioId(), 24, 28);
        g.drawString("tick: " + tick.tick() + " move: " + tick.moveType() + " dist: " + format(tick.distanceToGoal()), 24, 48);
        g.drawString("pos: " + format(tick.position().x()) + ", " + format(tick.position().y()) + ", " + format(tick.position().z()), 24, 68);
    }

    private void drawLine(Graphics2D g, Bounds bounds, Vec3d from, Vec3d to) {
        g.draw(new Line2D.Double(screenX(bounds, from), screenY(bounds, from), screenX(bounds, to), screenY(bounds, to)));
    }

    private void drawIsoLine(Graphics2D g, Bounds bounds, Vec3d from, Vec3d to) {
        double[] a = isoPoint(bounds, from.x(), from.y(), from.z());
        double[] b = isoPoint(bounds, to.x(), to.y(), to.z());
        g.draw(new Line2D.Double(a[0], a[1], b[0], b[1]));
    }

    private void drawPlayer(Graphics2D g, Bounds bounds, SimulationTick tick) {
        double x = screenX(bounds, tick.position());
        double y = screenY(bounds, tick.position());
        g.setColor(tick.stuck() ? STUCK : PLAYER);
        g.fill(new Ellipse2D.Double(x - 6.0, y - 6.0, 12.0, 12.0));
        g.setColor(TEXT);
        g.draw(new Ellipse2D.Double(x - 9.0, y - 9.0, 18.0, 18.0));
    }

    private void drawIsoPlayer(Graphics2D g, Bounds bounds, SimulationTick tick) {
        double[] point = isoPoint(bounds, tick.position().x(), tick.position().y(), tick.position().z());
        double x = point[0];
        double y = point[1];
        g.setColor(tick.stuck() ? STUCK : PLAYER);
        g.fill(new Ellipse2D.Double(x - 7.0, y - 16.0, 14.0, 14.0));
        g.setColor(TEXT);
        g.draw(new Line2D.Double(x, y - 2.0, x, y - 15.0));
        g.draw(new Ellipse2D.Double(x - 10.0, y - 19.0, 20.0, 20.0));
    }

    private double screenX(Bounds bounds, Vec3d value) {
        return screenX(bounds, value.x());
    }

    private double screenY(Bounds bounds, Vec3d value) {
        return screenY(bounds, value.z());
    }

    private double screenX(Bounds bounds, double x) {
        double span = Math.max(1.0, bounds.maxX() - bounds.minX());
        return transformX(48.0 + (x - bounds.minX()) / span * (getWidth() - 96.0));
    }

    private double screenY(Bounds bounds, double z) {
        double span = Math.max(1.0, bounds.maxZ() - bounds.minZ());
        return transformY(getHeight() - 48.0 - (z - bounds.minZ()) / span * (getHeight() - 96.0));
    }

    private double[] isoPoint(Bounds bounds, double x, double y, double z) {
        double centerX = (bounds.minX() + bounds.maxX()) * 0.5;
        double centerZ = (bounds.minZ() + bounds.maxZ()) * 0.5;
        double scale = isoScale(bounds);
        double localX = x - centerX;
        double localZ = z - centerZ;
        double isoX = (localX - localZ) * scale + getWidth() * 0.5;
        double groundY = (localX + localZ) * scale * 0.5 + getHeight() * 0.58;
        double isoY = groundY - (y - terrainMinY) * scale * 0.8;
        return new double[] { transformX(isoX), transformY(isoY) };
    }

    private double isoScale(Bounds bounds) {
        double span = Math.max(1.0, Math.max(bounds.maxX() - bounds.minX(), bounds.maxZ() - bounds.minZ()));
        return Math.clamp(Math.min(getWidth(), getHeight()) / (span * 1.55), 5.0, 22.0);
    }

    private double transformX(double value) {
        return value * viewZoom + panX;
    }

    private double transformY(double value) {
        return value * viewZoom + panY;
    }

    private final class CameraMouseHandler extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent event) {
            lastDrag = event.getPoint();
        }

        @Override
        public void mouseReleased(MouseEvent event) {
            lastDrag = null;
        }

        @Override
        public void mouseClicked(MouseEvent event) {
            if (event.getClickCount() >= 2) {
                resetCamera();
            }
        }

        @Override
        public void mouseDragged(MouseEvent event) {
            if (lastDrag == null) {
                lastDrag = event.getPoint();
                return;
            }
            Point point = event.getPoint();
            panX += point.x - lastDrag.x;
            panY += point.y - lastDrag.y;
            lastDrag = point;
            repaint();
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent event) {
            zoomAt(event.getPoint(), event.getWheelRotation());
        }

        private void zoomAt(Point point, int wheelRotation) {
            double oldZoom = viewZoom;
            double factor = Math.pow(WHEEL_ZOOM_FACTOR, -wheelRotation);
            double newZoom = Math.clamp(oldZoom * factor, MIN_ZOOM, MAX_ZOOM);
            if (Double.compare(oldZoom, newZoom) == 0) {
                return;
            }
            double ratio = newZoom / oldZoom;
            panX = point.x - (point.x - panX) * ratio;
            panY = point.y - (point.y - panY) * ratio;
            viewZoom = newZoom;
            repaint();
        }
    }

    private Color blockColor(ReplayBlock block) {
        Color base = switch (block.kind()) {
            case "water" -> WATER;
            case "climbable" -> CLIMBABLE;
            case "danger" -> DANGER;
            case "grass" -> GRASS;
            case "leaves" -> LEAVES;
            case "earth" -> EARTH;
            case "sand" -> SAND;
            case "stone" -> STONE;
            case "wood" -> WOOD;
            case "snow" -> SNOW;
            default -> SOLID;
        };
        int elevation = Math.clamp((block.y() - terrainMinY) * 5, -36, 52);
        return shade(base, elevation);
    }

    private static Color shade(Color color, int delta) {
        return new Color(
            Math.clamp(color.getRed() + delta, 0, 255),
            Math.clamp(color.getGreen() + delta, 0, 255),
            Math.clamp(color.getBlue() + delta, 0, 255));
    }

    private static String format(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    private static Bounds boundsFor(SimulationResult result) {
        if (result == null) {
            return null;
        }
        return Bounds.of(result.ticksTrace(), result.terrain());
    }

    private static List<ReplayBlock> sortedIsoTerrain(SimulationResult result) {
        if (result == null) {
            return List.of();
        }
        return result.terrain().stream()
            .sorted((left, right) -> Integer.compare(left.x() + left.z() + left.y(), right.x() + right.z() + right.y()))
            .toList();
    }

    private static int minTerrainY(SimulationResult result) {
        if (result == null) {
            return 0;
        }
        return result.terrain().stream()
            .mapToInt(ReplayBlock::y)
            .min()
            .orElse(0);
    }

    private enum Face {
        TOP,
        SOUTH,
        EAST
    }
}
