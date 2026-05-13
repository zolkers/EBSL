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

public enum EbslNodeType {
    START(EbslNodeCategory.FLOW),
    START_CHAIN(EbslNodeCategory.FLOW),
    EVENT_FUNCTION(EbslNodeCategory.FLOW),
    EVENT_CALL(EbslNodeCategory.FLOW),

    SET_VARIABLE(EbslNodeCategory.DATA),
    CHANGE_VARIABLE(EbslNodeCategory.DATA),
    CREATE_LIST(EbslNodeCategory.DATA),
    ADD_TO_LIST(EbslNodeCategory.DATA),
    REMOVE_FIRST_FROM_LIST(EbslNodeCategory.DATA),
    REMOVE_LAST_FROM_LIST(EbslNodeCategory.DATA),
    REMOVE_LIST_ITEM(EbslNodeCategory.DATA),
    LIST_ITEM(EbslNodeCategory.DATA),
    LIST_LENGTH(EbslNodeCategory.DATA),
    OPERATOR_MOD(EbslNodeCategory.DATA),
    OPERATOR_RANDOM(EbslNodeCategory.DATA),

    GOTO(EbslNodeCategory.WORLD),
    GOAL_NEAREST_BLOCK(EbslNodeCategory.WORLD),
    TRAVEL(EbslNodeCategory.WORLD),
    COME(EbslNodeCategory.WORLD),
    SPACE_MOB(EbslNodeCategory.WORLD),

    CONTROL_REPEAT(EbslNodeCategory.CONTROL),
    CONTROL_REPEAT_UNTIL(EbslNodeCategory.CONTROL),
    CONTROL_WAIT_UNTIL(EbslNodeCategory.CONTROL),
    CONTROL_FOREVER(EbslNodeCategory.CONTROL),
    CONTROL_IF(EbslNodeCategory.CONTROL),
    CONTROL_IF_ELSE(EbslNodeCategory.CONTROL),

    AIM_AT(EbslNodeCategory.PLAYER),
    AIM_AT_BLOCK(EbslNodeCategory.PLAYER),
    LOOK(EbslNodeCategory.PLAYER),
    WALK(EbslNodeCategory.PLAYER),
    JUMP(EbslNodeCategory.PLAYER),
    PRESS_KEY(EbslNodeCategory.PLAYER),
    CRAWL(EbslNodeCategory.PLAYER),
    CROUCH(EbslNodeCategory.PLAYER),
    SPRINT(EbslNodeCategory.PLAYER),
    STOP(EbslNodeCategory.PLAYER),
    SWING(EbslNodeCategory.PLAYER),
    USE(EbslNodeCategory.PLAYER),
    INTERACT(EbslNodeCategory.PLAYER),
    BREAK(EbslNodeCategory.PLAYER),
    BREAK_BLOCK(EbslNodeCategory.PLAYER),
    PLACE_HAND(EbslNodeCategory.PLAYER),

    WAIT(EbslNodeCategory.UTILITY),
    MESSAGE(EbslNodeCategory.UTILITY),
    NO_RENDER(EbslNodeCategory.UTILITY),
    STOP_CHAIN(EbslNodeCategory.UTILITY),
    STOP_ALL(EbslNodeCategory.UTILITY);

    private final EbslNodeCategory category;

    EbslNodeType(EbslNodeCategory category) {
        this.category = category;
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public EbslNodeCategory category() {
        return category;
    }

    public static EbslNodeType byId(String id) {
        String normalized = normalize(id);
        for (EbslNodeType type : values()) {
            if (type.id().equals(normalized)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown EBSL node: " + id);
    }

    public static List<String> ids() {
        return Arrays.stream(values()).map(EbslNodeType::id).toList();
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT).replace('-', '_');
    }
}
