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

package fr.riege.ebsl.common.feature.scripting.manager;

import fr.riege.ebsl.common.core.settings.*;
import fr.riege.ebsl.common.feature.scripting.EbslNode;
import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeType;
import fr.riege.ebsl.common.feature.scripting.registry.EbslNodeRegistry;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public final class EbslNodeFieldHelp {
    private static final Map<EbslNodeFieldId, String> DESCRIPTIONS = Map.ofEntries(
        Map.entry(EbslNodeFieldId.BLOCK_ID, "Block selector to search for, for example minecraft:oak_leaves, leaves, or wood&!crimson_stem."),
        Map.entry(EbslNodeFieldId.BLOCK, "Block selector expected by this action or sensor."),
        Map.entry(EbslNodeFieldId.SEARCH_RADIUS, "Maximum block scan radius around the player. Larger values are more expensive."),
        Map.entry(EbslNodeFieldId.REACH_RADIUS, "Maximum interaction distance used to choose a standing position near the block."),
        Map.entry(EbslNodeFieldId.TICKS, "How long the action runs. Supports tick suffixes such as 4t and second suffixes such as 1s."),
        Map.entry(EbslNodeFieldId.DURATION, "How long the input/action is held. Supports 4t for ticks and 1s for seconds."),
        Map.entry(EbslNodeFieldId.MAX_DURATION, "Safety timeout. The node stops even if the block was not broken yet."),
        Map.entry(EbslNodeFieldId.YAW, "Horizontal camera angle in degrees."),
        Map.entry(EbslNodeFieldId.PITCH, "Vertical camera angle in degrees."),
        Map.entry(EbslNodeFieldId.X, "World X coordinate."),
        Map.entry(EbslNodeFieldId.Y, "World Y coordinate."),
        Map.entry(EbslNodeFieldId.Z, "World Z coordinate."),
        Map.entry(EbslNodeFieldId.TOLERANCE, "Allowed distance before the condition counts as satisfied."),
        Map.entry(EbslNodeFieldId.DISTANCE, "Target distance or radius, depending on the node."),
        Map.entry(EbslNodeFieldId.RADIUS, "Maximum scan or targeting radius."),
        Map.entry(EbslNodeFieldId.SPEED, "Threshold speed used by the sensor."),
        Map.entry(EbslNodeFieldId.THRESHOLD, "Numeric threshold used by the sensor."),
        Map.entry(EbslNodeFieldId.DIRECTION, "Cardinal direction such as north, south, east, or west."),
        Map.entry(EbslNodeFieldId.KEY, "Input key name such as jump, attack, use, forward, sprint, or sneak."),
        Map.entry(EbslNodeFieldId.NAME, "Variable, function, list, or target name."),
        Map.entry(EbslNodeFieldId.VALUE, "Value to write or compare."),
        Map.entry(EbslNodeFieldId.DELTA, "Amount added to the current numeric value."),
        Map.entry(EbslNodeFieldId.VARIABLE, "Variable name that receives the result."),
        Map.entry(EbslNodeFieldId.LIST, "List variable name."),
        Map.entry(EbslNodeFieldId.INDEX, "Zero-based list index."),
        Map.entry(EbslNodeFieldId.MIN, "Minimum random value."),
        Map.entry(EbslNodeFieldId.MAX, "Maximum random value."),
        Map.entry(EbslNodeFieldId.MODE, "Mode or command option."),
        Map.entry(EbslNodeFieldId.TRACK, "Whether the task should keep tracking a moving target.")
    );
    private static final Map<ScopedDescriptionKey, String> SCOPED_DESCRIPTIONS = Map.ofEntries(
        Map.entry(new ScopedDescriptionKey(EbslNodeType.GOAL_NEAREST_BLOCK, EbslNodeFieldId.SEARCH_RADIUS), "How far to look for a matching block before deciding where to walk."),
        Map.entry(new ScopedDescriptionKey(EbslNodeType.GOAL_NEAREST_BLOCK, EbslNodeFieldId.REACH_RADIUS), "How close the chosen standing position must be to the block so it can be interacted with."),
        Map.entry(new ScopedDescriptionKey(EbslNodeType.AIM_AT_BLOCK, EbslNodeFieldId.SEARCH_RADIUS), "How far to scan for a matching block to aim at. Keep this small inside tight loops."),
        Map.entry(new ScopedDescriptionKey(EbslNodeType.AIM_AT_BLOCK, EbslNodeFieldId.TICKS), "How long the camera keeps adjusting toward the selected block."),
        Map.entry(new ScopedDescriptionKey(EbslNodeType.BREAK_BLOCK, EbslNodeFieldId.BLOCK), "Optional expected block selector. If set, breaking stops when the targeted block no longer matches it.")
    );

    private EbslNodeFieldHelp() {
    }

    public static String description(String command, Setting<?> setting) {
        return description(command, setting.id());
    }

    public static String description(String command, String fieldId) {
        EbslNodeFieldId knownField = EbslNodeFieldId.byId(fieldId);
        String scoped = scopedDescription(command, knownField);
        if (!scoped.isBlank()) {
            return scoped;
        }
        String description = knownField == null ? null : DESCRIPTIONS.get(knownField);
        if (description != null) {
            return description;
        }
        return "Parameter passed to the " + command + " node.";
    }

    public static String meta(Setting<?> setting) {
        StringBuilder builder = new StringBuilder(typeName(setting));
        builder.append(" | default ");
        builder.append(value(setting.defaultValue()));
        if (setting instanceof IntSetting s) {
            builder.append(" | ").append(s.min()).append("..").append(s.max());
        } else if (setting instanceof DoubleSetting s) {
            builder.append(" | ").append(trim(s.min())).append("..").append(trim(s.max()));
        } else if (setting instanceof EnumSetting<?> s) {
            builder.append(" | ");
            builder.append(List.of(s.enumType().getEnumConstants()).stream()
                .map(value -> value.name().toLowerCase(Locale.ROOT))
                .collect(Collectors.joining(", ")));
        }
        return builder.toString();
    }

    public static String signature(String command) {
        EbslNode node = EbslNodeRegistry.get(command);
        if (node == null || node.fields().isEmpty()) {
            return "";
        }
        return node.fields().stream()
            .map(field -> field.label() + ": " + field.defaultValue())
            .collect(Collectors.joining(" | "));
    }

    private static String scopedDescription(String command, EbslNodeFieldId settingId) {
        if (settingId == null) {
            return "";
        }
        try {
            EbslNodeType nodeType = EbslNodeType.byId(command);
            return SCOPED_DESCRIPTIONS.getOrDefault(new ScopedDescriptionKey(nodeType, settingId), "");
        } catch (IllegalArgumentException ignored) {
            return "";
        }
    }

    public static String typeName(Setting<?> setting) {
        if (setting instanceof IntSetting) return "integer";
        if (setting instanceof DoubleSetting) return "decimal";
        if (setting instanceof BooleanSetting) return "boolean";
        if (setting instanceof EnumSetting<?>) return "choice";
        if (setting instanceof ColorSetting) return "color";
        if (setting instanceof StringListSetting) return "list";
        if (setting instanceof StringSetting) return "text";
        return "value";
    }

    public static String value(Object value) {
        if (value instanceof Enum<?> e) {
            return e.name().toLowerCase(Locale.ROOT);
        }
        if (value instanceof Double d) {
            return trim(d);
        }
        if (value instanceof List<?> list) {
            return list.isEmpty() ? "empty" : list.stream().map(String::valueOf).collect(Collectors.joining(", "));
        }
        String text = String.valueOf(value);
        return text.isBlank() ? "empty" : text;
    }

    private static String trim(double value) {
        if (value == Math.rint(value)) {
            return Integer.toString((int) value);
        }
        return Double.toString(value);
    }

    private record ScopedDescriptionKey(EbslNodeType nodeType, EbslNodeFieldId fieldId) {
    }
}
