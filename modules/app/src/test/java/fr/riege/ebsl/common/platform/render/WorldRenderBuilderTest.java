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

package fr.riege.ebsl.common.platform.render;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class WorldRenderBuilderTest {
    @Test
    void filledBlockEmitsFullCubeInCameraLocalCoordinates() {
        RecordingRenderHandle handle = new RecordingRenderHandle(10.0, 20.0, 30.0);

        WorldRender.builder(handle)
            .argb(0x80406020)
            .filledBlock(11, 22, 33);

        assertEquals(1, handle.triangleBatches);
        assertEquals(12, handle.triangles);
        assertEquals(0x80406020, handle.argb);
        assertEquals(1.0, handle.firstX);
        assertEquals(2.0, handle.firstY);
        assertEquals(3.0, handle.firstZ);
    }

    @Test
    void lineUsesConfiguredWidthAndDepthMode() {
        RecordingRenderHandle handle = new RecordingRenderHandle(1.0, 2.0, 3.0);

        WorldRender.builder(handle)
            .lineWidth(2.5f)
            .throughWalls()
            .line(2.0, 4.0, 6.0, 3.0, 5.0, 7.0);

        assertEquals(1, handle.lineBatches);
        assertEquals(1, handle.lines);
        assertEquals(2.5f, handle.lineWidth);
        assertEquals(true, handle.ignoreDepth);
        assertEquals(1.0, handle.firstX);
        assertEquals(2.0, handle.firstY);
        assertEquals(3.0, handle.firstZ);
    }

    private static final class RecordingRenderHandle implements RenderHandle {
        private final double cameraX;
        private final double cameraY;
        private final double cameraZ;
        private int argb;
        private int lineBatches;
        private int triangleBatches;
        private int lines;
        private int triangles;
        private double firstX = Double.NaN;
        private double firstY = Double.NaN;
        private double firstZ = Double.NaN;
        private float lineWidth;
        private boolean ignoreDepth;

        private RecordingRenderHandle(double cameraX, double cameraY, double cameraZ) {
            this.cameraX = cameraX;
            this.cameraY = cameraY;
            this.cameraZ = cameraZ;
        }

        @Override
        public void beginLines(float r, float g, float b, float a) {
            lineBatches++;
            argb = pack(r, g, b, a);
        }

        @Override
        public void emitLine(double x1, double y1, double z1,
                             double x2, double y2, double z2,
                             float lineWidth) {
            lines++;
            this.lineWidth = lineWidth;
            recordFirst(x1, y1, z1);
        }

        @Override
        public void beginTriangles(float r, float g, float b, float a) {
            triangleBatches++;
            argb = pack(r, g, b, a);
        }

        @Override
        public void emitTriangle(double x1, double y1, double z1,
                                 double x2, double y2, double z2,
                                 double x3, double y3, double z3) {
            triangles++;
            recordFirst(x1, y1, z1);
        }

        @Override
        public void end(boolean ignoreDepth) {
            this.ignoreDepth = ignoreDepth;
        }

        @Override public double cameraX() { return cameraX; }
        @Override public double cameraY() { return cameraY; }
        @Override public double cameraZ() { return cameraZ; }

        private void recordFirst(double x, double y, double z) {
            if (Double.isNaN(firstX)) {
                firstX = x;
                firstY = y;
                firstZ = z;
            }
        }

        private static int pack(float r, float g, float b, float a) {
            return ((int) (a * 255.0f) << 24)
                | ((int) (r * 255.0f) << 16)
                | ((int) (g * 255.0f) << 8)
                | (int) (b * 255.0f);
        }
    }
}
