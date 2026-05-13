/*
 * This file is part of EBSL.
 *
 * EBSL is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EBSL is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package fr.riege.ebsl.common.platform.render;

public final class WorldRenderBuilder implements RenderStyleSink<WorldRenderBuilder> {
    private final RenderHandle handle;
    private RenderPaint paint = RenderPaint.SOLID_WHITE;
    private boolean ignoreDepth;
    private float lineWidth = 1.0f;

    WorldRenderBuilder(RenderHandle handle) {
        if (handle == null) {
            throw new NullPointerException("render handle required");
        }
        this.handle = handle;
    }

    @Override
    public WorldRenderBuilder paint(RenderPaint paint) {
        this.paint = paint != null ? paint : RenderPaint.SOLID_WHITE;
        return this;
    }

    @Override
    public WorldRenderBuilder lineWidth(float lineWidth) {
        this.lineWidth = Math.max(0.1f, lineWidth);
        return this;
    }

    @Override
    public WorldRenderBuilder ignoreDepth(boolean ignoreDepth) {
        this.ignoreDepth = ignoreDepth;
        return this;
    }

    public WorldRenderBuilder throughWalls() {
        return ignoreDepth(true);
    }

    public WorldRenderBuilder depthTested() {
        return ignoreDepth(false);
    }

    public WorldRenderBuilder line(double x1, double y1, double z1,
                                   double x2, double y2, double z2) {
        try (WorldRenderSession session = WorldRender.session(handle)
            .paint(paint)
            .lineWidth(lineWidth)
            .ignoreDepth(ignoreDepth)) {
            session.line(x1, y1, z1, x2, y2, z2);
        }
        return this;
    }

    public WorldRenderBuilder filledBlock(int x, int y, int z) {
        return filledBox(x, y, z, x + 1.0, y + 1.0, z + 1.0);
    }

    public WorldRenderBuilder wireBlock(int x, int y, int z) {
        return wireBox(x, y, z, x + 1.0, y + 1.0, z + 1.0);
    }

    public WorldRenderBuilder filledBox(double minX, double minY, double minZ,
                                        double maxX, double maxY, double maxZ) {
        try (WorldRenderSession session = WorldRender.session(handle)
            .paint(paint)
            .ignoreDepth(ignoreDepth)) {
            session.filledBox(minX, minY, minZ, maxX, maxY, maxZ);
        }
        return this;
    }

    public WorldRenderBuilder wireBox(double minX, double minY, double minZ,
                                      double maxX, double maxY, double maxZ) {
        try (WorldRenderSession session = WorldRender.session(handle)
            .paint(paint)
            .lineWidth(lineWidth)
            .ignoreDepth(ignoreDepth)) {
            session.wireBox(minX, minY, minZ, maxX, maxY, maxZ);
        }
        return this;
    }
}
