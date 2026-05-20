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

import fr.riege.ebsl.tools.pathfindersim.replay.SimulationResult;
import fr.riege.ebsl.tools.pathfindersim.ui.render.IsometricReplayRenderer;
import fr.riege.ebsl.tools.pathfindersim.ui.render.ReplayCamera;
import fr.riege.ebsl.tools.pathfindersim.ui.render.ReplayHudRenderer;
import fr.riege.ebsl.tools.pathfindersim.ui.render.ReplayPalette;
import fr.riege.ebsl.tools.pathfindersim.ui.render.ReplayRenderContext;
import fr.riege.ebsl.tools.pathfindersim.ui.render.ReplayRenderModel;
import fr.riege.ebsl.tools.pathfindersim.ui.render.ReplayRenderer;
import fr.riege.ebsl.tools.pathfindersim.ui.render.TopDownReplayRenderer;

import javax.swing.JPanel;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

final class ReplayCanvas extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final double DEFAULT_YAW_RADIANS = Math.toRadians(45.0);
    private static final double FULL_ROTATION_RADIANS = Math.PI * 2.0;
    private static final double MIN_ZOOM = 0.35;
    private static final double MAX_ZOOM = 6.0;
    private static final double WHEEL_ZOOM_FACTOR = 1.12;
    private static final double DRAG_ROTATION_RADIANS_PER_PIXEL = 0.008;

    private final transient ReplayRenderer topDownRenderer = new TopDownReplayRenderer();
    private final transient ReplayRenderer isometricRenderer = new IsometricReplayRenderer();
    private final transient ReplayHudRenderer hudRenderer = new ReplayHudRenderer();

    private transient SimulationResult result;
    private transient ReplayRenderModel renderModel = ReplayRenderModel.of(null, DEFAULT_YAW_RADIANS);
    private transient Point lastDrag;
    private int frame;
    private double yawRadians = DEFAULT_YAW_RADIANS;
    private boolean isometric = true;
    private boolean rotating;
    private double viewZoom = 1.0;
    private double panX;
    private double panY;

    ReplayCanvas() {
        setBackground(ReplayPalette.BACKGROUND);
        CameraMouseHandler camera = new CameraMouseHandler();
        addMouseListener(camera);
        addMouseMotionListener(camera);
        addMouseWheelListener(camera);
    }

    void setResult(SimulationResult result) {
        this.result = result;
        this.renderModel = ReplayRenderModel.of(result, yawRadians);
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

    void setYawDegrees(int degrees) {
        yawRadians = Math.toRadians(Math.floorMod(degrees, 360));
        renderModel.updateYaw(yawRadians);
        repaint();
    }

    void resetCamera() {
        viewZoom = 1.0;
        panX = 0.0;
        panY = 0.0;
        yawRadians = DEFAULT_YAW_RADIANS;
        renderModel.updateYaw(yawRadians);
        lastDrag = null;
        rotating = false;
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
        g.setColor(ReplayPalette.GRID);
        int spacing = 32;
        for (int x = 0; x < getWidth(); x += spacing) {
            g.drawLine(x, 0, x, getHeight());
        }
        for (int y = 0; y < getHeight(); y += spacing) {
            g.drawLine(0, y, getWidth(), y);
        }
    }

    private static void paintEmpty(Graphics2D g) {
        g.setColor(ReplayPalette.TEXT);
        g.setFont(g.getFont().deriveFont(Font.BOLD, 18f));
        g.drawString("No replay loaded", 28, 42);
    }

    private void paintReplay(Graphics2D g) {
        int cappedFrame = Math.min(frame, result.ticksTrace().size() - 1);
        ReplayCamera camera = new ReplayCamera(viewZoom, panX, panY, yawRadians, getWidth(), getHeight());
        ReplayRenderContext context = new ReplayRenderContext(result, renderModel, camera, cappedFrame);
        currentRenderer().render(g, context);
        hudRenderer.render(g, context);
    }

    private ReplayRenderer currentRenderer() {
        return isometric ? isometricRenderer : topDownRenderer;
    }

    private final class CameraMouseHandler extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent event) {
            rotating = event.getButton() == MouseEvent.BUTTON2;
            lastDrag = event.getPoint();
        }

        @Override
        public void mouseReleased(MouseEvent event) {
            lastDrag = null;
            rotating = false;
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
            if (rotating) {
                rotateView(point);
            } else {
                panView(point);
            }
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

        private void panView(Point point) {
            panX += (long) point.x - (long) lastDrag.x;
            panY += (long) point.y - (long) lastDrag.y;
        }

        private void rotateView(Point point) {
            long deltaX = (long) point.x - (long) lastDrag.x;
            yawRadians = normalizeYaw(yawRadians + deltaX * DRAG_ROTATION_RADIANS_PER_PIXEL);
            renderModel.updateYaw(yawRadians);
        }
    }

    private static double normalizeYaw(double radians) {
        double normalized = radians % FULL_ROTATION_RADIANS;
        return normalized < 0.0 ? normalized + FULL_ROTATION_RADIANS : normalized;
    }
}
