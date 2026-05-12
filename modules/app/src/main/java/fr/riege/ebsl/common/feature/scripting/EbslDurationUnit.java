package fr.riege.ebsl.common.feature.scripting;

public enum EbslDurationUnit {
    MILLISECOND("ms"),
    SECOND("s"),
    TICK("t");

    private final String suffix;

    EbslDurationUnit(String suffix) {
        this.suffix = suffix;
    }

    public static EbslDurationUnit fromToken(String token) {
        if (token == null) {
            return null;
        }
        for (EbslDurationUnit unit : values()) {
            if (token.endsWith(unit.suffix)) {
                return unit;
            }
        }
        return null;
    }

    public static boolean hasDurationSuffix(String token) {
        EbslDurationUnit unit = fromToken(token);
        if (unit == null) {
            return false;
        }
        try {
            unit.numericValue(token);
            return true;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    public double numericValue(String token) {
        return Double.parseDouble(token.substring(0, token.length() - suffix.length()));
    }

    public int toTicks(double value) {
        return switch (this) {
            case MILLISECOND -> Math.max(1, (int) Math.ceil(value / 50.0));
            case SECOND -> Math.max(1, (int) Math.ceil(value * 20.0));
            case TICK -> Math.max(0, (int) Math.floor(value));
        };
    }
}
