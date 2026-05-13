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

package fr.riege.ebsl.common.feature.scripting.enums;

import java.util.Locale;

public enum EbslCardinalDirection {
    NORTH(180.0f),
    SOUTH(0.0f),
    WEST(90.0f),
    EAST(-90.0f);

    private final float yaw;

    EbslCardinalDirection(float yaw) {
        this.yaw = yaw;
    }

    public boolean matches(float playerYaw) {
        return Math.abs(normalizeYaw(playerYaw - yaw)) <= 45.0f;
    }

    public static EbslCardinalDirection byToken(String token) {
        if (token == null) {
            return null;
        }
        try {
            return valueOf(token.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static float normalizeYaw(float yaw) {
        float value = yaw % 360.0f;
        if (value < -180.0f) {
            value += 360.0f;
        }
        if (value > 180.0f) {
            value -= 360.0f;
        }
        return value;
    }
}
