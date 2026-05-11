package fr.riege.ebsl.common.feature.scripting.manager;

import fr.riege.ebsl.common.core.settings.BooleanSetting;
import fr.riege.ebsl.common.core.settings.ColorSetting;
import fr.riege.ebsl.common.core.settings.DoubleSetting;
import fr.riege.ebsl.common.core.settings.EnumSetting;
import fr.riege.ebsl.common.core.settings.IntSetting;
import fr.riege.ebsl.common.core.settings.Setting;
import fr.riege.ebsl.common.core.settings.StringListSetting;
import fr.riege.ebsl.common.core.settings.StringSetting;
import fr.riege.ebsl.common.feature.scripting.EbslNode;
import fr.riege.ebsl.common.feature.scripting.registry.EbslNodeRegistry;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public final class EbslNodeFieldHelp {
    private static final Map<String, String> DESCRIPTIONS = Map.ofEntries(
        Map.entry("block_id", "Block selector to search for, for example minecraft:oak_leaves, leaves, or wood&!crimson_stem."),
        Map.entry("block", "Block selector expected by this action or sensor."),
        Map.entry("search_radius", "Maximum block scan radius around the player. Larger values are more expensive."),
        Map.entry("reach_radius", "Maximum interaction distance used to choose a standing position near the block."),
        Map.entry("ticks", "How long the action runs. Supports tick suffixes such as 4t and second suffixes such as 1s."),
        Map.entry("duration", "How long the input/action is held. Supports 4t for ticks and 1s for seconds."),
        Map.entry("max_duration", "Safety timeout. The node stops even if the block was not broken yet."),
        Map.entry("yaw", "Horizontal camera angle in degrees."),
        Map.entry("pitch", "Vertical camera angle in degrees."),
        Map.entry("x", "World X coordinate."),
        Map.entry("y", "World Y coordinate."),
        Map.entry("z", "World Z coordinate."),
        Map.entry("tolerance", "Allowed distance before the condition counts as satisfied."),
        Map.entry("distance", "Target distance or radius, depending on the node."),
        Map.entry("radius", "Maximum scan or targeting radius."),
        Map.entry("speed", "Threshold speed used by the sensor."),
        Map.entry("threshold", "Numeric threshold used by the sensor."),
        Map.entry("direction", "Cardinal direction such as north, south, east, or west."),
        Map.entry("key", "Input key name such as jump, attack, use, forward, sprint, or sneak."),
        Map.entry("name", "Variable, function, list, or target name."),
        Map.entry("value", "Value to write or compare."),
        Map.entry("delta", "Amount added to the current numeric value."),
        Map.entry("variable", "Variable name that receives the result."),
        Map.entry("list", "List variable name."),
        Map.entry("index", "Zero-based list index."),
        Map.entry("min", "Minimum random value."),
        Map.entry("max", "Maximum random value."),
        Map.entry("mode", "Mode or command option."),
        Map.entry("track", "Whether the task should keep tracking a moving target.")
    );

    private EbslNodeFieldHelp() {
    }

    public static String description(String command, Setting<?> setting) {
        return description(command, setting.id());
    }

    public static String description(String command, String fieldId) {
        String scoped = scopedDescription(command, fieldId);
        if (!scoped.isBlank()) {
            return scoped;
        }
        String description = DESCRIPTIONS.get(fieldId);
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

    private static String scopedDescription(String command, String settingId) {
        String key = (command == null ? "" : command) + "." + settingId;
        return switch (key) {
            case "goal_nearest_block.search_radius" -> "How far to look for a matching block before deciding where to walk.";
            case "goal_nearest_block.reach_radius" -> "How close the chosen standing position must be to the block so it can be interacted with.";
            case "aim_at_block.search_radius" -> "How far to scan for a matching block to aim at. Keep this small inside tight loops.";
            case "aim_at_block.ticks" -> "How long the camera keeps adjusting toward the selected block.";
            case "break_block.block" -> "Optional expected block selector. If set, breaking stops when the targeted block no longer matches it.";
            default -> "";
        };
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
}
