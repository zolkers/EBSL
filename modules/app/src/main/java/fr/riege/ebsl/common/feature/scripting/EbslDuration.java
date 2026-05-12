package fr.riege.ebsl.common.feature.scripting;

import java.util.Locale;

public final class EbslDuration {
    private EbslDuration() {
    }

    public static int ticks(String token) {
        String value = token.toLowerCase(Locale.ROOT).trim();
        try {
            EbslDurationUnit unit = EbslDurationUnit.fromToken(value);
            if (unit != null) {
                return unit.toTicks(unit.numericValue(value));
            }
            return Math.max(0, (int) Math.floor(Double.parseDouble(value)));
        } catch (NumberFormatException ignored) {
            return 1;
        }
    }
}
