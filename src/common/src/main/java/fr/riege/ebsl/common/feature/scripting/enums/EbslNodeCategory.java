package fr.riege.ebsl.common.feature.scripting.enums;

import java.util.Locale;

public enum EbslNodeCategory {
    FLOW,
    CONTROL,
    DATA,
    WORLD,
    PLAYER,
    INTERFACE,
    SENSOR,
    UTILITY,
    PARAMETER;

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }
}
