package fr.riege.ebsl.common.feature.scripting;

import java.util.Locale;

public final class EbslDuration {
    private EbslDuration() {
    }

    public static int ticks(String token) {
        String value = token.toLowerCase(Locale.ROOT).trim();
        try {
            if (value.endsWith("ms")) {
                return Math.max(1, (int) Math.ceil(Double.parseDouble(value.substring(0, value.length() - 2)) / 50.0));
            }
            if (value.endsWith("s")) {
                return Math.max(1, (int) Math.ceil(Double.parseDouble(value.substring(0, value.length() - 1)) * 20.0));
            }
            if (value.endsWith("t")) {
                return Math.max(0, (int) Math.floor(Double.parseDouble(value.substring(0, value.length() - 1))));
            }
            return Math.max(0, (int) Math.floor(Double.parseDouble(value)));
        } catch (NumberFormatException ignored) {
            return 1;
        }
    }
}
