/*
 * This file is part of EBSL.
 *
 * EBSL is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EBSL is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

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
