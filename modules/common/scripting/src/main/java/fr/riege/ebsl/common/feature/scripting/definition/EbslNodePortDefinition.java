/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Copyright (C) 2026 Victor Riegert and EBSL contributors
 *
 * This file is part of EBSL.
 *
 * EBSL is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EBSL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EBSL. If not, see <https://www.gnu.org/licenses/>.
 */

package fr.riege.ebsl.common.feature.scripting.definition;

import fr.riege.ebsl.common.feature.scripting.manager.EbslGraphPort;
import fr.riege.ebsl.common.feature.scripting.manager.EbslGraphPortDirection;
import fr.riege.ebsl.common.feature.scripting.manager.EbslGraphPortKind;

import java.util.Objects;

public record EbslNodePortDefinition(
    String id,
    String label,
    EbslGraphPortDirection direction,
    EbslGraphPortKind kind,
    EbslValueType valueType,
    boolean multiple
) {
    public EbslNodePortDefinition {
        id = requireText(id, "id");
        label = label == null || label.isBlank() ? id : label.trim();
        Objects.requireNonNull(direction, "direction");
        kind = kind == null ? EbslGraphPortKind.FLOW : kind;
        valueType = valueType == null ? EbslValueType.ANY : valueType;
    }

    public static EbslNodePortDefinition input(String id, EbslValueType valueType) {
        return new EbslNodePortDefinition(id, id, EbslGraphPortDirection.INPUT, EbslGraphPortKind.DATA, valueType, false);
    }

    public static EbslNodePortDefinition output(String id, EbslValueType valueType) {
        return new EbslNodePortDefinition(id, id, EbslGraphPortDirection.OUTPUT, EbslGraphPortKind.DATA, valueType, true);
    }

    public static EbslNodePortDefinition flowInput(String id, String label) {
        return new EbslNodePortDefinition(id, label, EbslGraphPortDirection.INPUT, EbslGraphPortKind.FLOW, EbslValueType.FLOW, false);
    }

    public static EbslNodePortDefinition flowOutput(String id, String label) {
        return new EbslNodePortDefinition(id, label, EbslGraphPortDirection.OUTPUT, EbslGraphPortKind.FLOW, EbslValueType.FLOW, true);
    }

    public EbslGraphPort toGraphPort() {
        return new EbslGraphPort(id, label, direction, kind, multiple);
    }

    private static String requireText(String value, String name) {
        String checked = Objects.requireNonNull(value, name).trim();
        if (checked.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return checked;
    }
}
