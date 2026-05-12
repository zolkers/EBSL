package fr.riege.ebsl.common.feature.scripting.nodes;

import java.util.Locale;

enum NoRenderDirective {
    DEFAULT_DISABLE(""),
    ON("on"),
    TRUE("true"),
    DISABLE("disable"),
    OFF("off");

    private final String token;

    NoRenderDirective(String token) {
        this.token = token;
    }

    static boolean disablesRendering(String rawToken) {
        String normalized = rawToken == null ? "" : rawToken.trim().toLowerCase(Locale.ROOT);
        for (NoRenderDirective directive : values()) {
            if (directive.token.equals(normalized)) {
                return true;
            }
        }
        return false;
    }
}
