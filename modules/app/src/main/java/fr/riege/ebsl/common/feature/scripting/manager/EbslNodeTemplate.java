package fr.riege.ebsl.common.feature.scripting.manager;

import fr.riege.ebsl.common.feature.scripting.EbslNode;
import fr.riege.ebsl.common.feature.scripting.EbslNodeField;
import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeCategory;
import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeType;
import fr.riege.ebsl.common.feature.scripting.registry.EbslNodeRegistry;

import java.util.Locale;

public record EbslNodeTemplate(
    String command,
    EbslNodeCategory category,
    String title,
    String description,
    String argsHint,
    String sampleArgs
) {
    private static final String GOAL_PREFIX = "goal_";
    private static final String SENSOR_PREFIX = "sensor_";
    private static final String DURATION_ARG = "duration";
    private static final String ITEMS_SAMPLE = "items";

    public String sampleLine() {
        return sampleArgs.isBlank() ? command() : command() + " " + sampleArgs;
    }

    public boolean matches(String filter) {
        String normalized = filter == null ? "" : filter.trim().toLowerCase(Locale.ROOT).replace('-', '_');
        return normalized.isBlank()
            || command().contains(normalized)
            || title.toLowerCase(Locale.ROOT).contains(normalized)
            || description.toLowerCase(Locale.ROOT).contains(normalized);
    }

    public static EbslNodeTemplate of(String command) {
        EbslNode node = EbslNodeRegistry.get(command);
        if (node != null) {
            return of(node);
        }
        try {
            return of(EbslNodeType.byId(command));
        } catch (RuntimeException exception) {
            return template(command, EbslNodeCategory.UTILITY, prettify(command), "Custom script node.", "args", "");
        }
    }

    public static EbslNodeTemplate of(EbslNode node) {
        if (node.id().startsWith(SENSOR_PREFIX)) {
            return dynamic(node);
        }
        EbslNodeType type = typeOrNull(node.id());
        if (type != null) {
            return withNodeFields(of(type), node);
        }
        return dynamic(node);
    }

    private static EbslNodeTemplate dynamic(EbslNode node) {
        EbslNodeCategory category = dynamicCategory(node.id());
        String argsHint = fieldsHint(node);
        String sampleArgs = fieldsSample(node);
        String title = dynamicTitle(node.id());
        String description = dynamicDescription(node.id(), category);
        return template(node.id(), category, title, description, argsHint, sampleArgs);
    }

    public static EbslNodeTemplate of(EbslNodeType type) {
        return switch (type) {
            case START -> template(type, "Start", "Entry point for a script chain.", "", "");
            case START_CHAIN -> template(type, "Start Chain", "Entry point for a script branch.", "", "");
            case EVENT_FUNCTION -> template(type, "Function", "Define a reusable script block.", "name block", "main {\n}");
            case EVENT_CALL -> template(type, "Call Function", "Call a reusable script block.", "function", "main");
            case WAIT -> template(type, "Wait", "Pause the chain for a duration.", DURATION_ARG, "1s");
            case CONTROL_IF -> template(type, "If", "Run a block when a condition is true.", "condition block", "true {\n}");
            case CONTROL_IF_ELSE -> template(type, "If Else", "Run one of two blocks from a condition.", "condition then else", "true {\n} else {\n}");
            case CONTROL_REPEAT -> template(type, "Repeat", "Run a block a fixed number of times.", "count block", "3 {\n}");
            case CONTROL_REPEAT_UNTIL -> template(type, "Repeat Until", "Repeat a block until a condition is true.", "condition block", "true {\n}");
            case CONTROL_FOREVER -> template(type, "Forever", "Run a block continuously.", "block", "{\n}");
            case CONTROL_WAIT_UNTIL -> template(type, "Wait Until", "Pause until a sensor/condition is true.", "condition", "true");
            case MESSAGE -> template(type, "Message", "Send a chat/status message.", "text", "\"hello\"");
            case NO_RENDER -> template(type, "No Render", "Disable EBSL world/path/task rendering.", "[on|false]", "");
            case GOTO -> template(type, "Goto", "Navigate to explicit coordinates.", "x y z", "0 64 0");
            case GOAL_NEAREST_BLOCK -> template(type, "Nearest Block", "Find the nearest matching loaded block and path within reach radius.", "block_id search_radius reach_radius", "minecraft:oak_leaves 32 4");
            case TRAVEL -> template(type, "Travel", "Travel toward a world target.", "x y z", "0 64 0");
            case COME -> template(type, "Come", "Ask the bot to come to the player.", "", "");
            case SPACE_MOB -> template(type, "Space Mob", "Use the SpaceMob task from a script node.", "on|off [name] [distance] [tolerance] [radius] [track]", "on closest 3 0.35 32 track");
            case AIM_AT -> template(type, "Aim At", "Smoothly aim at world coordinates.", "x y z [" + DURATION_ARG + "]", "0 64 0 8t");
            case AIM_AT_BLOCK -> template(type, "Aim At Block", "Find and aim at the nearest matching loaded block.", "block_id [search_radius] [" + DURATION_ARG + "]", "minecraft:oak_leaves 32 10t");
            case WALK -> template(type, "Walk", "Hold movement forward for a duration.", DURATION_ARG, "1s");
            case JUMP -> template(type, "Jump", "Press jump for a duration.", DURATION_ARG, "2t");
            case CRAWL -> template(type, "Crawl", "Hold crawl/sneak-style movement.", DURATION_ARG, "1s");
            case CROUCH -> template(type, "Crouch", "Hold crouch for a duration.", DURATION_ARG, "1s");
            case SPRINT -> template(type, "Sprint", "Hold sprint for a duration.", DURATION_ARG, "1s");
            case PRESS_KEY -> template(type, "Press Key", "Press a named input key.", "key " + DURATION_ARG, "jump 2t");
            case LOOK -> template(type, "Look", "Rotate view toward a direction/target.", "args", "");
            case BREAK -> template(type, "Break", "Hold attack/break input.", DURATION_ARG, "2t");
            case BREAK_BLOCK -> template(type, "Break Block", "Hold attack until the targeted block disappears or changes.", "[block_id] [max_" + DURATION_ARG + "]", "minecraft:oak_leaves");
            case USE -> template(type, "Use", "Use item/interact input.", DURATION_ARG, "2t");
            case INTERACT -> template(type, "Interact", "Interact with targeted block/entity.", DURATION_ARG, "2t");
            case SWING -> template(type, "Swing", "Swing the player hand.", "", "");
            case PLACE_HAND -> template(type, "Place Hand", "Place using current hand.", DURATION_ARG, "2t");
            case SET_VARIABLE -> template(type, "Set Variable", "Assign a variable.", "name value", "wood minecraft:oak_log");
            case CHANGE_VARIABLE -> template(type, "Change Variable", "Add a delta to a variable.", "name delta", "value 1");
            case CREATE_LIST -> template(type, "Create List", "Create an empty list variable.", "name", ITEMS_SAMPLE);
            case ADD_TO_LIST -> template(type, "Add To List", "Append a value to a list.", "list value", ITEMS_SAMPLE + " stone");
            case REMOVE_FIRST_FROM_LIST -> template(type, "Remove First", "Remove the first list value.", "list", ITEMS_SAMPLE);
            case REMOVE_LAST_FROM_LIST -> template(type, "Remove Last", "Remove the last list value.", "list", ITEMS_SAMPLE);
            case REMOVE_LIST_ITEM -> template(type, "Remove Item", "Remove an indexed list value.", "list index", ITEMS_SAMPLE + " 0");
            case LIST_ITEM -> template(type, "List Item", "Read an indexed list item.", "variable list index", "item " + ITEMS_SAMPLE + " 0");
            case LIST_LENGTH -> template(type, "List Length", "Read a list length.", "variable list", "length " + ITEMS_SAMPLE);
            case OPERATOR_RANDOM -> template(type, "Random", "Generate a random numeric value.", "variable min max", "random 0 10");
            case OPERATOR_MOD -> template(type, "Modulo", "Compute modulo.", "variable left right", "result 10 2");
            case STOP -> template(type, "Stop", "Stop player/path action.", "", "");
            case STOP_CHAIN -> template(type, "Stop Chain", "Stop current script chain.", "", "");
            case STOP_ALL -> template(type, "Stop All", "Stop all running chains/actions.", "", "");
            default -> template(type, prettify(type.id()), type.category().id() + " node.", "args", "");
        };
    }

    private static EbslNodeTemplate template(EbslNodeType type, String title, String description, String argsHint, String sampleArgs) {
        return template(type.id(), type.category(), title, description, argsHint, sampleArgs);
    }

    private static EbslNodeTemplate template(String command, EbslNodeCategory category, String title, String description, String argsHint, String sampleArgs) {
        return new EbslNodeTemplate(command, category, title, description, argsHint, sampleArgs);
    }

    private static EbslNodeTemplate withNodeFields(EbslNodeTemplate base, EbslNode node) {
        if (node.fields().isEmpty()) {
            return base;
        }
        return template(base.command(), base.category(), base.title(), base.description(), fieldsHint(node), fieldsSample(node));
    }

    private static EbslNodeType typeOrNull(String command) {
        try {
            return EbslNodeType.byId(command);
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private static String fieldsHint(EbslNode node) {
        return node.fields().stream()
            .map(EbslNodeField::id)
            .reduce((a, b) -> a + " " + b)
            .orElse("");
    }

    private static String fieldsSample(EbslNode node) {
        return node.fields().stream()
            .map(EbslNodeField::defaultValue)
            .reduce((a, b) -> a + " " + b)
            .orElse("");
    }

    private static EbslNodeCategory dynamicCategory(String command) {
        if (command.startsWith(GOAL_PREFIX)) {
            return EbslNodeCategory.WORLD;
        }
        if (command.startsWith(SENSOR_PREFIX)) {
            return EbslNodeCategory.SENSOR;
        }
        return EbslNodeCategory.UTILITY;
    }

    private static String dynamicTitle(String command) {
        if (command.startsWith(GOAL_PREFIX)) {
            return prettify(command.substring(GOAL_PREFIX.length()));
        }
        if (command.startsWith(SENSOR_PREFIX)) {
            return prettify(command.substring(SENSOR_PREFIX.length()));
        }
        return prettify(command);
    }

    private static String dynamicDescription(String command, EbslNodeCategory category) {
        if (command.startsWith(GOAL_PREFIX)) {
            return "Registered pathfinding goal.";
        }
        if (command.startsWith(SENSOR_PREFIX)) {
            return "Read a sensor into a script variable.";
        }
        return category.id() + " node.";
    }

    private static String prettify(String id) {
        StringBuilder builder = new StringBuilder();
        for (String part : id.split("_")) {
            if (part.isBlank()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return builder.toString();
    }
}
