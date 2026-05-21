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

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record EbslNodeFieldDefinition(
    String id,
    String label,
    EbslValueType type,
    boolean required,
    boolean expressionEnabled,
    String defaultValue,
    List<EbslFieldOption> options,
    Map<String, String> metadata
) {
    public EbslNodeFieldDefinition {
        id = requireText(id, "id");
        label = label == null || label.isBlank() ? id : label.trim();
        type = type == null ? EbslValueType.STRING : type;
        defaultValue = defaultValue == null ? "" : defaultValue;
        options = List.copyOf(options == null ? List.of() : options);
        metadata = Map.copyOf(metadata == null ? Map.of() : metadata);
    }

    public static EbslNodeFieldDefinition required(String id, EbslValueType type) {
        return new EbslNodeFieldDefinition(id, id, type, true, true, "", List.of(), Map.of());
    }

    public static EbslNodeFieldDefinition optional(String id, EbslValueType type, String defaultValue) {
        return new EbslNodeFieldDefinition(id, id, type, false, true, defaultValue, List.of(), Map.of());
    }

    public EbslNodeFieldDefinition withLabel(String nextLabel) {
        return new EbslNodeFieldDefinition(id, nextLabel, type, required, expressionEnabled, defaultValue, options, metadata);
    }

    public EbslNodeFieldDefinition withoutExpressions() {
        return new EbslNodeFieldDefinition(id, label, type, required, false, defaultValue, options, metadata);
    }

    public EbslNodeFieldDefinition withOptions(List<EbslFieldOption> nextOptions) {
        return new EbslNodeFieldDefinition(id, label, type, required, expressionEnabled, defaultValue, nextOptions, metadata);
    }

    public EbslNodeFieldDefinition withMetadata(Map<String, String> nextMetadata) {
        return new EbslNodeFieldDefinition(id, label, type, required, expressionEnabled, defaultValue, options, nextMetadata);
    }

    private static String requireText(String value, String name) {
        String checked = Objects.requireNonNull(value, name).trim();
        if (checked.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return checked;
    }
}
