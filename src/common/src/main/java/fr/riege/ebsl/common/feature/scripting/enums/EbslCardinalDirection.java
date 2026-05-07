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
