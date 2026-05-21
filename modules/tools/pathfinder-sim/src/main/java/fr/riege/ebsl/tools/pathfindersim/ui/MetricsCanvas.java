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
    private static final int LEFT_AXIS = 62;
    private static final int RIGHT_PAD = 14;
    private static final int TITLE_HEIGHT = 24;
    private static final int BOTTOM_AXIS = 30;
    private static final int GRID_LINES = 4;
    private static final int X_TICKS = 3;

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
        List<SimulationTick> ticks = result.ticksTrace();
        double maxDistance = upperBound(ticks.stream().mapToDouble(SimulationTick::distanceToGoal).max().orElse(1.0));
        double maxSpeed = upperBound(ticks.stream().mapToDouble(tick -> horizontalSpeed(tick.velocity())).max().orElse(0.01));
        ChartArea distance = drawChartFrame(g, new ChartSpec(
            "Distance to goal", PAD, PAD, getWidth() - PAD * 2, topHeight, maxDistance, lastTick(ticks)));
        ChartArea speed = drawChartFrame(g, new ChartSpec(
            "Horizontal speed", PAD, PAD * 2 + topHeight, getWidth() - PAD * 2, topHeight, maxSpeed, lastTick(ticks)));
        drawDistance(g, distance, maxDistance);
        drawSpeed(g, speed, maxSpeed);
        drawLegend(g);
    }

    private ChartArea drawChartFrame(Graphics2D g, ChartSpec spec) {
        ChartArea area = new ChartArea(
            spec.x + LEFT_AXIS,
            spec.y + TITLE_HEIGHT,
            Math.max(32, spec.width - LEFT_AXIS - RIGHT_PAD),
            Math.max(32, spec.height - TITLE_HEIGHT - BOTTOM_AXIS));
        g.setColor(GRID);
        g.setFont(g.getFont().deriveFont(Font.PLAIN, 11f));
        for (int i = 0; i <= GRID_LINES; i++) {
            double ratio = i / (double) GRID_LINES;
            int yy = area.y + Math.toIntExact(Math.round(area.height * ratio));
            g.drawLine(area.x, yy, area.x + area.width, yy);
            drawRightAligned(g, formatAxisValue(spec.maxValue * (1.0 - ratio)), area.x - 8, yy + 4);
        }
        for (int i = 0; i < X_TICKS; i++) {
            double ratio = i / (double) (X_TICKS - 1);
            int xx = area.x + Math.toIntExact(Math.round(area.width * ratio));
            g.drawLine(xx, area.y, xx, area.y + area.height + 4);
            String tick = Integer.toString(Math.toIntExact(Math.round(spec.lastTick * ratio)));
            drawCentered(g, tick, xx, area.y + area.height + 20);
        }
        g.drawRect(area.x, area.y, area.width, area.height);
        g.setColor(TEXT);
        g.setFont(g.getFont().deriveFont(Font.BOLD, 14f));
        g.drawString(spec.title, spec.x, spec.y + 14);
        g.setFont(g.getFont().deriveFont(Font.PLAIN, 11f));
        drawCentered(g, "tick", area.x + area.width / 2, area.y + area.height + 30);
        return area;
    }

    private void drawDistance(Graphics2D g, ChartArea area, double max) {
        List<SimulationTick> ticks = result.ticksTrace();
        g.setColor(DISTANCE);
        drawSeries(g, ticks.size(), area, max, index -> ticks.get(index).distanceToGoal());
        g.setColor(STUCK);
        drawStuckMarkers(g, ticks, area);
    }

    private void drawSpeed(Graphics2D g, ChartArea area, double max) {
        List<SimulationTick> ticks = result.ticksTrace();
        g.setColor(SPEED);
        drawSeries(g, ticks.size(), area, max, index -> horizontalSpeed(ticks.get(index).velocity()));
        g.setColor(STUCK);
        drawStuckMarkers(g, ticks, area);
    }

    private void drawSeries(Graphics2D g, int count, ChartArea area, double max, ValueAt valueAt) {
        if (count < 2) {
            return;
        }
        g.setStroke(new BasicStroke(2.0f));
        for (int i = 1; i < count; i++) {
            double px = area.x + (i - 1) / (double) (count - 1) * area.width;
            double cx = area.x + i / (double) (count - 1) * area.width;
            double py = yForValue(area, valueAt.value(i - 1), max);
            double cy = yForValue(area, valueAt.value(i), max);
            g.draw(new Line2D.Double(px, py, cx, cy));
        }
    }

    private void drawStuckMarkers(Graphics2D g, List<SimulationTick> ticks, ChartArea area) {
        if (ticks.size() < 2) {
            return;
        }
        for (int i = 0; i < ticks.size(); i++) {
            if (ticks.get(i).stuck()) {
                int markerX = area.x + Math.toIntExact(Math.round(i / (double) (ticks.size() - 1) * area.width));
                g.drawLine(markerX, area.y, markerX, area.y + area.height);
            }
        }
    }

    private void drawLegend(Graphics2D g) {
        g.setColor(TEXT);
        g.setFont(g.getFont().deriveFont(Font.PLAIN, 13f));
        g.drawString(String.format(Locale.ROOT,
            "ticks=%d  nodes=%d/%d  stuck=%d  recovery=%d  lateral=%.2f/%.2f  backward=%d  final=%.2f",
            result.ticks(),
            result.navigationNodes(),
            result.rawNodes(),
            result.metrics().stuckTicks(),
            result.metrics().recoveryAttempts(),
            result.metrics().averageLateralError(),
            result.metrics().maxLateralError(),
            result.metrics().backwardTicks(),
            result.metrics().finalDistance()), PAD, getHeight() - 20);
    }

    private static double horizontalSpeed(Vec3d velocity) {
        return Math.hypot(velocity.x(), velocity.z());
    }

    private static double yForValue(ChartArea area, double value, double max) {
        double ratio = max <= 0.0 ? 0.0 : Math.clamp(value / max, 0.0, 1.0);
        return area.y + area.height - ratio * area.height;
    }

    private static double upperBound(double value) {
        double sanitized = Math.max(0.01, value);
        double scale = Math.pow(10.0, Math.floor(Math.log10(sanitized)));
        return Math.ceil(sanitized / scale) * scale;
    }

    private static int lastTick(List<SimulationTick> ticks) {
        return ticks.isEmpty() ? 0 : ticks.getLast().tick();
    }

    private static void drawRightAligned(Graphics2D g, String text, int x, int y) {
        g.drawString(text, x - g.getFontMetrics().stringWidth(text), y);
    }

    private static void drawCentered(Graphics2D g, String text, int x, int y) {
        g.drawString(text, x - g.getFontMetrics().stringWidth(text) / 2, y);
    }

    private static String formatAxisValue(double value) {
        if (value >= 10.0) {
            return String.format(Locale.ROOT, "%.0f", value);
        }
        if (value >= 1.0) {
            return String.format(Locale.ROOT, "%.1f", value);
        }
        return String.format(Locale.ROOT, "%.2f", value);
    }

    private record ChartArea(int x, int y, int width, int height) {
    }

    private record ChartSpec(String title, int x, int y, int width, int height, double maxValue, int lastTick) {
    }

    @FunctionalInterface
    private interface ValueAt {
        double value(int index);
    }
}
