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

package fr.riege.ebsl.common.feature.scripting.conditions;

import java.util.List;

public enum EbslConditionOperatorType {
    EQUALS("equals", "==", "="),
    NOT_EQUALS("not_equals", "!=", "not"),
    GREATER_THAN("greater_than", ">"),
    LESS_THAN("less_than", "<"),
    GREATER_OR_EQUAL("greater_or_equal", ">="),
    LESS_OR_EQUAL("less_or_equal", "<="),
    AND("and"),
    OR("or"),
    XOR("xor");

    private final String id;
    private final List<String> aliases;

    EbslConditionOperatorType(String id, String... aliases) {
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
