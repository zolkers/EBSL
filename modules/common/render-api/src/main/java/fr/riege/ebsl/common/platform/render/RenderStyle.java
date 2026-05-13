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

public record RenderStyle(RenderPaint paint, float lineWidth, boolean ignoreDepth) {
    public static final RenderStyle DEFAULT = builder().build();

    public static Builder builder() {
        return new Builder();
    }

    public RenderStyle {
        paint = paint != null ? paint : RenderPaint.SOLID_WHITE;
        lineWidth = Math.max(0.1f, lineWidth);
    }

    public RenderStyle(RenderColor color, float lineWidth, boolean ignoreDepth) {
        this(RenderPaint.solid(color), lineWidth, ignoreDepth);
    }

    public RenderColor color() {
        return paint.baseColor();
    }

    public Builder toBuilder() {
        return builder()
            .paint(paint)
            .lineWidth(lineWidth)
            .ignoreDepth(ignoreDepth);
    }

    public static final class Builder implements RenderStyleSink<Builder> {
        private RenderPaint paint = RenderPaint.SOLID_WHITE;
        private float lineWidth = 1.0f;
        private boolean ignoreDepth;

        private Builder() {
        }

        @Override
        public Builder paint(RenderPaint paint) {
            this.paint = paint != null ? paint : RenderPaint.SOLID_WHITE;
            return this;
        }

        public Builder rainbow(float alpha, float saturation, float brightness, float cycles, float speed) {
            return paint(RenderPaint.rainbow(alpha, saturation, brightness, cycles, speed));
        }

        @Override
        public Builder lineWidth(float lineWidth) {
            this.lineWidth = lineWidth;
            return this;
        }

        @Override
        public Builder ignoreDepth(boolean ignoreDepth) {
            this.ignoreDepth = ignoreDepth;
            return this;
        }

        public Builder throughWalls() {
            return ignoreDepth(true);
        }

        public Builder depthTested() {
            return ignoreDepth(false);
        }

        public RenderStyle build() {
            return new RenderStyle(paint, lineWidth, ignoreDepth);
        }
    }
}
