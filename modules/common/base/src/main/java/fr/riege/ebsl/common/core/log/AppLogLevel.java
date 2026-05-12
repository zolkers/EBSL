package fr.riege.ebsl.common.core.log;

import java.util.Locale;

public enum AppLogLevel {
    ERROR,
    FATAL,
    WARN,
    INFO,
    DEBUG,
    TRACE,
    OTHER;

    public static AppLogLevel fromName(String name) {
        if (name == null || name.isBlank()) {
            return OTHER;
        }
        try {
            return valueOf(name.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return OTHER;
        }
    }

    public String label() {
        return this == OTHER ? "LOG" : name();
    }
}
