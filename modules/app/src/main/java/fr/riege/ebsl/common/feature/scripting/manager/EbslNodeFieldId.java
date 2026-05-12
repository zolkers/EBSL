package fr.riege.ebsl.common.feature.scripting.manager;

import java.util.Locale;

enum EbslNodeFieldId {
    BLOCK_ID("block_id"),
    BLOCK("block"),
    SEARCH_RADIUS("search_radius"),
    REACH_RADIUS("reach_radius"),
    TICKS("ticks"),
    DURATION("duration"),
    MAX_DURATION("max_duration"),
    YAW("yaw"),
    PITCH("pitch"),
    X("x"),
    Y("y"),
    Z("z"),
    TOLERANCE("tolerance"),
    DISTANCE("distance"),
    RADIUS("radius"),
    SPEED("speed"),
    THRESHOLD("threshold"),
    DIRECTION("direction"),
    KEY("key"),
    NAME("name"),
    VALUE("value"),
    DELTA("delta"),
    VARIABLE("variable"),
    LIST("list"),
    INDEX("index"),
    MIN("min"),
    MAX("max"),
    MODE("mode"),
    TRACK("track");

    private final String id;

    EbslNodeFieldId(String id) {
        this.id = id;
    }

    static EbslNodeFieldId byId(String id) {
        String normalized = normalize(id);
        for (EbslNodeFieldId fieldId : values()) {
            if (fieldId.id.equals(normalized)) {
                return fieldId;
            }
        }
        return null;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT).replace('-', '_');
    }
}
