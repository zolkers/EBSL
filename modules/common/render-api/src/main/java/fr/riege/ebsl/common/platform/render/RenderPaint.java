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

/**
 * Describes how colors are sampled along a rendered primitive.
 *
 * <p>The sealed paint types support solid colors, gradients, and animated rainbow effects through one sampling contract.</p>
 */
public sealed interface RenderPaint permits RenderPaint.Solid, RenderPaint.Gradient, RenderPaint.Rainbow {
    RenderPaint SOLID_WHITE = solid(RenderColor.WHITE);

    /**
     * Samples the paint color at the supplied normalized progress value.
 *
     * @param progress the normalized progress value, usually between 0 and 1
     * @return the value defined by this contract
     */
    RenderColor colorAt(float progress);

    /**
     * Returns the representative color used when a single preview color is needed.
 *
     * @return the value defined by this contract
     */
    default RenderColor baseColor() {
        return colorAt(0.0f);
    }

    /**
     * Creates a render paint instance from the supplied color parameters.
 *
     * @param color the color to apply
     * @return the value defined by this contract
     */
    static RenderPaint solid(RenderColor color) {
        return new Solid(color != null ? color : RenderColor.WHITE);
    }

    /**
     * Creates a render paint instance from the supplied color parameters.
 *
     * @param from the first value or starting position
     * @param to the second value or ending position
     * @return the value defined by this contract
     */
    static RenderPaint gradient(RenderColor from, RenderColor to) {
        return new Gradient(
            from != null ? from : RenderColor.WHITE,
            to != null ? to : RenderColor.WHITE);
    }

    /**
     * Creates a render paint instance from the supplied color parameters.
 *
     * @return the value defined by this contract
     */
    static RenderPaint rainbow() {
        return rainbow(0.95f, 1.0f, 1.0f, 0.82f, 0.18f);
    }

    /**
     * Creates a render paint instance from the supplied color parameters.
 *
     * @param alpha the alpha component to apply
     * @return the value defined by this contract
     */
    static RenderPaint rainbow(float alpha) {
        return rainbow(alpha, 1.0f, 1.0f, 0.82f, 0.18f);
    }

    /**
     * Creates a render paint instance from the supplied color parameters.
 *
     * @param alpha the alpha component to apply
     * @param saturation the color saturation to apply
     * @param brightness the color brightness to apply
     * @param cycles the number of rainbow cycles along the primitive
     * @param speed the animation speed multiplier
     * @return the value defined by this contract
     */
    static RenderPaint rainbow(float alpha, float saturation, float brightness, float cycles, float speed) {
        return new Rainbow(
            Math.clamp(alpha, 0.0f, 1.0f),
            Math.clamp(saturation, 0.0f, 1.0f),
            Math.clamp(brightness, 0.0f, 1.0f),
            Math.max(0.01f, cycles),
            speed);
    }

    record Solid(RenderColor color) implements RenderPaint {
        public Solid {
            color = color != null ? color : RenderColor.WHITE;
        }

        @Override
        public RenderColor colorAt(float progress) {
            return color;
        }
    }

    record Gradient(RenderColor from, RenderColor to) implements RenderPaint {
        public Gradient {
            from = from != null ? from : RenderColor.WHITE;
            to = to != null ? to : RenderColor.WHITE;
        }

        @Override
        public RenderColor colorAt(float progress) {
            return from.lerp(to, progress);
        }
    }

    record Rainbow(float alpha, float saturation, float brightness, float cycles, float speed) implements RenderPaint {
        public Rainbow {
            alpha = Math.clamp(alpha, 0.0f, 1.0f);
            saturation = Math.clamp(saturation, 0.0f, 1.0f);
            brightness = Math.clamp(brightness, 0.0f, 1.0f);
            cycles = Math.max(0.01f, cycles);
        }

        @Override
        public RenderColor colorAt(float progress) {
            float seconds = System.nanoTime() / 1_000_000_000.0f;
            return RenderColor.hsv(progress * cycles + seconds * speed, saturation, brightness, alpha);
        }
    }
}
