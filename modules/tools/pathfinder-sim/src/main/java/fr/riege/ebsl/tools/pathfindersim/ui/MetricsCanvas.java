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
import fr.riege.ebsl.tools.pathfindersim.replay.SimulationResult;
import fr.riege.ebsl.tools.pathfindersim.replay.SimulationTick;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.util.List;
import java.util.Locale;

final class MetricsCanvas extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final Color BACKGROUND = new Color(17, 21, 26);
    private static final Color GRID = new Color(49, 57, 67);
    private static final Color TEXT = new Color(226, 232, 240);
    private static final Color DISTANCE = new Color(99, 179, 237);
    private static final Color SPEED = new Color(104, 211, 145);
    private static final Color STUCK = new Color(248, 113, 113);
    private static final int PAD = 48;

    private transient SimulationResult result;

    MetricsCanvas() {
        setBackground(BACKGROUND);
    }

    void setResult(SimulationResult result) {
        this.result = result;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (result == null || result.ticksTrace().isEmpty()) {
            paintEmpty(g);
        } else {
            paintCharts(g);
        }
        g.dispose();
    }

    private void paintEmpty(Graphics2D g) {
        g.setColor(TEXT);
        g.setFont(g.getFont().deriveFont(Font.BOLD, 18f));
        g.drawString("No metrics loaded", 28, 42);
    }

    private void paintCharts(Graphics2D g) {
        int topHeight = (getHeight() - PAD * 3) / 2;
        drawChartFrame(g, "Distance to goal", PAD, PAD, getWidth() - PAD * 2, topHeight);
        drawChartFrame(g, "Horizontal speed", PAD, PAD * 2 + topHeight, getWidth() - PAD * 2, topHeight);
        drawDistance(g, PAD, PAD, getWidth() - PAD * 2, topHeight);
        drawSpeed(g, PAD, PAD * 2 + topHeight, getWidth() - PAD * 2, topHeight);
        drawLegend(g);
    }

    private void drawChartFrame(Graphics2D g, String title, int x, int y, int width, int height) {
        g.setColor(GRID);
        for (int i = 0; i <= 4; i++) {
            int yy = y + Math.toIntExact(Math.round(height * (i / 4.0)));
            g.drawLine(x, yy, x + width, yy);
        }
        g.drawRect(x, y, width, height);
        g.setColor(TEXT);
        g.setFont(g.getFont().deriveFont(Font.BOLD, 14f));
        g.drawString(title, x, y - 12);
    }

    private void drawDistance(Graphics2D g, int x, int y, int width, int height) {
        List<SimulationTick> ticks = result.ticksTrace();
        double max = ticks.stream().mapToDouble(SimulationTick::distanceToGoal).max().orElse(1.0);
        g.setColor(DISTANCE);
        drawSeries(g, ticks.size(), x, y, width, height, index -> ticks.get(index).distanceToGoal() / max);
        g.setColor(STUCK);
        drawStuckMarkers(g, ticks, x, y, width, height);
    }

    private void drawSpeed(Graphics2D g, int x, int y, int width, int height) {
        List<SimulationTick> ticks = result.ticksTrace();
        double max = Math.max(0.01, ticks.stream().mapToDouble(tick -> horizontalSpeed(tick.velocity())).max().orElse(0.01));
        g.setColor(SPEED);
        drawSeries(g, ticks.size(), x, y, width, height, index -> horizontalSpeed(ticks.get(index).velocity()) / max);
        g.setColor(STUCK);
        drawStuckMarkers(g, ticks, x, y, width, height);
    }

    private void drawSeries(Graphics2D g, int count, int x, int y, int width, int height, ValueAt valueAt) {
        if (count < 2) {
            return;
        }
        g.setStroke(new BasicStroke(2.0f));
        for (int i = 1; i < count; i++) {
            double px = x + (i - 1) / (double) (count - 1) * width;
            double cx = x + i / (double) (count - 1) * width;
            double py = y + height - Math.clamp(valueAt.value(i - 1), 0.0, 1.0) * height;
            double cy = y + height - Math.clamp(valueAt.value(i), 0.0, 1.0) * height;
            g.draw(new Line2D.Double(px, py, cx, cy));
        }
    }

    private void drawStuckMarkers(Graphics2D g, List<SimulationTick> ticks, int x, int y, int width, int height) {
        if (ticks.size() < 2) {
            return;
        }
        for (int i = 0; i < ticks.size(); i++) {
            if (ticks.get(i).stuck()) {
                int markerX = x + Math.toIntExact(Math.round(i / (double) (ticks.size() - 1) * width));
                g.drawLine(markerX, y, markerX, y + height);
            }
        }
    }

    private void drawLegend(Graphics2D g) {
        g.setColor(TEXT);
        g.setFont(g.getFont().deriveFont(Font.PLAIN, 13f));
        g.drawString(String.format(Locale.ROOT,
            "ticks=%d  nodes=%d/%d  stuck=%d ticks  best=%.2f final=%.2f",
            result.ticks(),
            result.navigationNodes(),
            result.rawNodes(),
            result.metrics().stuckTicks(),
            result.metrics().bestDistance(),
            result.metrics().finalDistance()), PAD, getHeight() - 20);
    }

    private static double horizontalSpeed(Vec3d velocity) {
        return Math.hypot(velocity.x(), velocity.z());
    }

    @FunctionalInterface
    private interface ValueAt {
        double value(int index);
    }
}
