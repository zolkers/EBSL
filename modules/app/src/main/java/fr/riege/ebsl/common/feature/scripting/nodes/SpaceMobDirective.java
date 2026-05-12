package fr.riege.ebsl.common.feature.scripting.nodes;

import java.util.Locale;

enum SpaceMobDirective {
    ON,
    OFF,
    STOP,
    NAME,
    TRACK,
    CLOSEST;

    static SpaceMobDirective byToken(String token) {
        if (token == null) {
            return null;
        }
        try {
            return valueOf(token.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
