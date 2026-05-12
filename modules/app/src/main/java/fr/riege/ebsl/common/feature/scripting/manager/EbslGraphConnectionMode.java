package fr.riege.ebsl.common.feature.scripting.manager;

import java.util.Locale;

public enum EbslGraphConnectionMode {
    FLOW("flow"),
    EACH_INPUT("each_input");

    private final String id;

    EbslGraphConnectionMode(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public static EbslGraphConnectionMode byId(String id) {
        String normalized = id == null ? "" : id.trim().toLowerCase(Locale.ROOT).replace('-', '_');
        for (EbslGraphConnectionMode mode : values()) {
            if (mode.id.equals(normalized) || mode.name().toLowerCase(Locale.ROOT).equals(normalized)) {
                return mode;
            }
        }
        return FLOW;
    }
}
