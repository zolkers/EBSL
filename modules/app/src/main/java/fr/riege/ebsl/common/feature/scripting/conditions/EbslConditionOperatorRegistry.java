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

import fr.riege.ebsl.common.core.registry.IRegistry;
import fr.riege.ebsl.common.core.registry.MapRegistry;
import fr.riege.ebsl.common.feature.scripting.runtime.EbslScriptRuntime;
import java.util.Locale;

public final class EbslConditionOperatorRegistry {
    private static final IRegistry<String, EbslConditionOperator> OPERATORS = new MapRegistry<>(null);

    static {
        register(EbslConditionOperatorType.EQUALS, (runtime, left, right) -> String.valueOf(left).equalsIgnoreCase(String.valueOf(right)));
        register(EbslConditionOperatorType.NOT_EQUALS, (runtime, left, right) -> !String.valueOf(left).equalsIgnoreCase(String.valueOf(right)));
        register(EbslConditionOperatorType.GREATER_THAN, (runtime, left, right) -> runtime.number(left) > runtime.number(right));
        register(EbslConditionOperatorType.LESS_THAN, (runtime, left, right) -> runtime.number(left) < runtime.number(right));
        register(EbslConditionOperatorType.GREATER_OR_EQUAL, (runtime, left, right) -> runtime.number(left) >= runtime.number(right));
        register(EbslConditionOperatorType.LESS_OR_EQUAL, (runtime, left, right) -> runtime.number(left) <= runtime.number(right));
        register(EbslConditionOperatorType.AND, (runtime, left, right) -> runtime.truthy(left) && runtime.truthy(right));
        register(EbslConditionOperatorType.OR, (runtime, left, right) -> runtime.truthy(left) || runtime.truthy(right));
        register(EbslConditionOperatorType.XOR, (runtime, left, right) -> runtime.truthy(left) ^ runtime.truthy(right));
    }

    private EbslConditionOperatorRegistry() {
    }

    public static boolean evaluate(String token, EbslScriptRuntime runtime, Object left, Object right) {
        EbslConditionOperator operator = OPERATORS.get(normalize(token));
        return operator != null && operator.evaluate(runtime, left, right);
    }

    private static void register(EbslConditionOperatorType type, EbslConditionOperator operator) {
        OPERATORS.register(normalize(type.id()), operator);
        for (String alias : type.aliases()) {
            OPERATORS.register(normalize(alias), operator);
        }
    }

    private static String normalize(String token) {
        return token == null ? "" : token.trim().toLowerCase(Locale.ROOT).replace('-', '_');
    }
}
