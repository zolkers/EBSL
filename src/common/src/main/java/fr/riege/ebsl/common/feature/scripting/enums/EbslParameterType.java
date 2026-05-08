package fr.riege.ebsl.common.feature.scripting.enums;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public enum EbslParameterType {
    COORDINATE,
    BLOCK,
    ITEM,
    VILLAGER_TRADE,
    ENTITY,
    PLAYER,
    WAYPOINT,
    SCHEMATIC,
    INVENTORY_SLOT,
    MESSAGE,
    DURATION,
    AMOUNT,
    BOOLEAN,
    HAND,
    GUI,
    KEY,
    MOUSE_BUTTON,
    RANGE,
    DISTANCE,
    DIRECTION,
    BLOCK_FACE,
    ROTATION,
    PLACE_TARGET,
    CLOSEST;

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static EbslParameterType byId(String id) {
        String normalized = normalize(id);
        for (EbslParameterType type : values()) {
            if (type.id().equals(normalized)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown EBSL parameter type: " + id);
    }

    public static List<String> ids() {
        return Arrays.stream(values()).map(EbslParameterType::id).toList();
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT).replace('-', '_');
    }
}
