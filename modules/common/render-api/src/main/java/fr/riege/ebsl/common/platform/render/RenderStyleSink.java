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
 * Provides a fluent style-writing contract for render builders.
 *
 * <p>Implementations accept paint updates and return their own type so callers can chain style operations fluently.</p>
 */
public interface RenderStyleSink<T extends RenderStyleSink<T>> {
    T paint(RenderPaint paint);

    T lineWidth(float lineWidth);

    T ignoreDepth(boolean ignoreDepth);

    /**
     * Applies a solid color style and returns this sink for chaining.
 *
     * @param color the color to apply
     * @return the value defined by this contract
     */
    default T color(RenderColor color) {
        return paint(RenderPaint.solid(color));
    }

    /**
     * Applies a solid ARGB color style and returns this sink for chaining.
 *
     * @param argb the ARGB color value
     * @return the value defined by this contract
     */
    default T argb(int argb) {
        return color(RenderColor.argb(argb));
    }

    /**
     * Applies a gradient paint style and returns this sink for chaining.
 *
     * @param from the first value or starting position
     * @param to the second value or ending position
     * @return the value defined by this contract
     */
    default T gradient(RenderColor from, RenderColor to) {
        return paint(RenderPaint.gradient(from, to));
    }

    /**
     * Applies a gradient ARGB paint style and returns this sink for chaining.
 *
     * @param from the first value or starting position
     * @param to the second value or ending position
     * @return the value defined by this contract
     */
    default T gradientArgb(int from, int to) {
        return gradient(RenderColor.argb(from), RenderColor.argb(to));
    }

    /**
     * Applies a rainbow paint style and returns this sink for chaining.
 *
     * @return the value defined by this contract
     */
    default T rainbow() {
        return paint(RenderPaint.rainbow());
    }

    /**
     * Applies a rainbow paint style and returns this sink for chaining.
 *
     * @param alpha the alpha component to apply
     * @return the value defined by this contract
     */
    default T rainbow(float alpha) {
        return paint(RenderPaint.rainbow(alpha));
    }
}
