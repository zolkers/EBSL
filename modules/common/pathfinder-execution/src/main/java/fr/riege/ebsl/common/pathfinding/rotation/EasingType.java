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
package fr.riege.ebsl.common.pathfinding.rotation;

public enum EasingType {
    LINEAR(EasingType::linear),

    EASE_IN_SINE(EasingType::easeInSine),
    EASE_OUT_SINE(EasingType::easeOutSine),
    EASE_IN_OUT_SINE(EasingType::easeInOutSine),

    EASE_IN_QUAD(EasingType::easeInQuad),
    EASE_OUT_QUAD(EasingType::easeOutQuad),
    EASE_IN_OUT_QUAD(EasingType::easeInOutQuad),

    EASE_IN_CUBIC(EasingType::easeInCubic),
    EASE_OUT_CUBIC(EasingType::easeOutCubic),
    EASE_IN_OUT_CUBIC(EasingType::easeInOutCubic),

    EASE_IN_QUART(EasingType::easeInQuart),
    EASE_OUT_QUART(EasingType::easeOutQuart),
    EASE_IN_OUT_QUART(EasingType::easeInOutQuart),

    EASE_IN_QUINT(EasingType::easeInQuint),
    EASE_OUT_QUINT(EasingType::easeOutQuint),
    EASE_IN_OUT_QUINT(EasingType::easeInOutQuint),

    EASE_IN_EXPO(EasingType::easeInExpo),
    EASE_OUT_EXPO(EasingType::easeOutExpo),
    EASE_IN_OUT_EXPO(EasingType::easeInOutExpo),

    EASE_IN_CIRC(EasingType::easeInCirc),
    EASE_OUT_CIRC(EasingType::easeOutCirc),
    EASE_IN_OUT_CIRC(EasingType::easeInOutCirc),

    EASE_IN_BACK(EasingType::easeInBack),
    EASE_OUT_BACK(EasingType::easeOutBack),
    EASE_IN_OUT_BACK(EasingType::easeInOutBack);

    private static final float HALF = 0.5f;
    private static final float BACK_C1 = 1.70158f;
    private static final float BACK_C2 = BACK_C1 * 1.525f;
    private static final float BACK_C3 = BACK_C1 + 1.0f;

    private final Curve curve;

    EasingType(Curve curve) {
        this.curve = curve;
    }

    public float ease(float t) {
        return curve.apply(Math.clamp(t, 0f, 1f));
    }

    public float apply(float from, float to, float progress) {
        return from + (to - from) * ease(progress);
    }

    private static float linear(float t) {
        return t;
    }

    private static float easeInSine(float t) {
        return (float) (1.0 - Math.cos(t * Math.PI / 2.0));
    }

    private static float easeOutSine(float t) {
        return (float) Math.sin(t * Math.PI / 2.0);
    }

    private static float easeInOutSine(float t) {
        return (float) (-(Math.cos(Math.PI * t) - 1.0) / 2.0);
    }

    private static float easeInQuad(float t) {
        return t * t;
    }

    private static float easeOutQuad(float t) {
        return 1.0f - (1.0f - t) * (1.0f - t);
    }

    private static float easeInOutQuad(float t) {
        if (t < HALF) {
            return 2.0f * t * t;
        }
        float x = -2.0f * t + 2.0f;
        return 1.0f - x * x / 2.0f;
    }

    private static float easeInCubic(float t) {
        return t * t * t;
    }

    private static float easeOutCubic(float t) {
        float x = 1.0f - t;
        return 1.0f - x * x * x;
    }

    private static float easeInOutCubic(float t) {
        if (t < HALF) {
            return 4.0f * t * t * t;
        }
        float x = -2.0f * t + 2.0f;
        return 1.0f - x * x * x / 2.0f;
    }

    private static float easeInQuart(float t) {
        return t * t * t * t;
    }

    private static float easeOutQuart(float t) {
        float x = 1.0f - t;
        return 1.0f - x * x * x * x;
    }

    private static float easeInOutQuart(float t) {
        if (t < HALF) {
            return 8.0f * t * t * t * t;
        }
        float x = -2.0f * t + 2.0f;
        return 1.0f - x * x * x * x / 2.0f;
    }

    private static float easeInQuint(float t) {
        return t * t * t * t * t;
    }

    private static float easeOutQuint(float t) {
        float x = 1.0f - t;
        return 1.0f - x * x * x * x * x;
    }

    private static float easeInOutQuint(float t) {
        if (t < HALF) {
            return 16.0f * t * t * t * t * t;
        }
        float x = -2.0f * t + 2.0f;
        return 1.0f - x * x * x * x * x / 2.0f;
    }

    private static float easeInExpo(float t) {
        return t == 0.0f ? 0.0f : (float) Math.pow(2.0, 10.0 * t - 10.0);
    }

    private static float easeOutExpo(float t) {
        return t == 1.0f ? 1.0f : (float) (1.0 - Math.pow(2.0, -10.0 * t));
    }

    private static float easeInOutExpo(float t) {
        if (t == 0.0f || t == 1.0f) {
            return t;
        }
        if (t < HALF) {
            return (float) (Math.pow(2.0, 20.0 * t - 10.0) / 2.0);
        }
        return (float) ((2.0 - Math.pow(2.0, -20.0 * t + 10.0)) / 2.0);
    }

    private static float easeInCirc(float t) {
        return (float) (1.0 - Math.sqrt(1.0 - t * t));
    }

    private static float easeOutCirc(float t) {
        float x = t - 1.0f;
        return (float) Math.sqrt(1.0 - x * x);
    }

    private static float easeInOutCirc(float t) {
        if (t < HALF) {
            float x = 2.0f * t;
            return (float) ((1.0 - Math.sqrt(1.0 - x * x)) / 2.0);
        }
        float x = -2.0f * t + 2.0f;
        return (float) ((Math.sqrt(1.0 - x * x) + 1.0) / 2.0);
    }

    private static float easeInBack(float t) {
        return BACK_C3 * t * t * t - BACK_C1 * t * t;
    }

    private static float easeOutBack(float t) {
        float x = t - 1.0f;
        return 1.0f + BACK_C3 * x * x * x + BACK_C1 * x * x;
    }

    private static float easeInOutBack(float t) {
        if (t < HALF) {
            float x = 2.0f * t;
            return x * x * ((BACK_C2 + 1.0f) * x - BACK_C2) / 2.0f;
        }
        float x = 2.0f * t - 2.0f;
        return (x * x * ((BACK_C2 + 1.0f) * x + BACK_C2) + 2.0f) / 2.0f;
    }

    /**
     * Applies an easing curve to normalized progress.

     *

     * <p>Curves are pure mathematical callbacks used by rotation interpolation.</p>

     */
    @FunctionalInterface
    private interface Curve {
        /**
         * Applies this operation to the supplied value or context.
 *
         * @param t the normalized progress value
         * @return the value defined by this contract
         */
        float apply(float t);
    }
}
