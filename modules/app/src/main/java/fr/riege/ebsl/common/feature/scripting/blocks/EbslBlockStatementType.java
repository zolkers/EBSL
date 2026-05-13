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
package fr.riege.ebsl.common.feature.scripting.blocks;

import java.util.List;

public enum EbslBlockStatementType {
    EVENT_FUNCTION("event_function"),
    IF("if", "control_if", "control_if_else"),
    REPEAT("repeat", "control_repeat"),
    FOREVER("forever", "control_forever"),
    REPEAT_UNTIL("repeat_until", "control_repeat_until");

    private final String id;
    private final List<String> aliases;

    EbslBlockStatementType(String id, String... aliases) {
        this.id = id;
        this.aliases = List.of(aliases);
    }

    public String id() {
        return id;
    }

    public List<String> aliases() {
        return aliases;
    }
}
