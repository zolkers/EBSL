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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EbslNodeDefinitionRegistry {
    private final Map<String, EbslNodeDefinition> definitions;

    public EbslNodeDefinitionRegistry(Collection<EbslNodeDefinition> definitions) {
        Map<String, EbslNodeDefinition> values = new LinkedHashMap<>();
        if (definitions != null) {
            for (EbslNodeDefinition definition : definitions) {
                if (values.put(definition.type(), definition) != null) {
                    throw new IllegalArgumentException("Duplicate EBSL node definition: " + definition.type());
                }
            }
        }
        this.definitions = Map.copyOf(values);
    }

    public static EbslNodeDefinitionRegistry empty() {
        return new EbslNodeDefinitionRegistry(List.of());
    }

    public static EbslNodeDefinitionRegistry of(EbslNodeDefinition... definitions) {
        return new EbslNodeDefinitionRegistry(definitions == null ? List.of() : List.of(definitions));
    }

    public EbslNodeDefinition definition(String type) {
        return definitions.get(type == null ? "" : type.trim());
    }

    public boolean contains(String type) {
        return definition(type) != null;
    }

    public List<EbslNodeDefinition> all() {
        return definitions.values().stream()
            .sorted((first, second) -> first.label().compareToIgnoreCase(second.label()))
            .toList();
    }

    public List<EbslNodeDefinition> byGroup(EbslNodeGroup group) {
        return all().stream().filter(definition -> definition.group() == group).toList();
    }
}
