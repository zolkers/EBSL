package fr.riege.ebsl.common.feature.scripting.manager;

import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeCategory;
import fr.riege.ebsl.common.feature.scripting.enums.EbslNodeType;

import java.util.Locale;

public record EbslNodeTemplate(
    EbslNodeType type,
    String title,
    String description,
    String argsHint,
    String sampleArgs
) {
    public String command() {
        return type.id();
    }

    public String sampleLine() {
        return sampleArgs.isBlank() ? command() : command() + " " + sampleArgs;
    }

    public EbslNodeCategory category() {
        return type.category();
    }

    public boolean matches(String filter) {
        String normalized = filter == null ? "" : filter.trim().toLowerCase(Locale.ROOT).replace('-', '_');
        return normalized.isBlank()
            || command().contains(normalized)
            || title.toLowerCase(Locale.ROOT).contains(normalized)
            || description.toLowerCase(Locale.ROOT).contains(normalized);
    }

    public static EbslNodeTemplate of(EbslNodeType type) {
        return switch (type) {
            case START -> template(type, "Start", "Entry point for a script chain.", "", "");
            case WAIT -> template(type, "Wait", "Pause the chain for a duration.", "duration", "1s");
            case CONTROL_WAIT_UNTIL -> template(type, "Wait Until", "Pause until a sensor/condition is true.", "condition args", "");
            case MESSAGE -> template(type, "Message", "Send a chat/status message.", "text", "\"hello\"");
            case GOTO -> template(type, "Goto", "Navigate to explicit coordinates.", "x y z", "0 64 0");
            case TRAVEL -> template(type, "Travel", "Travel toward a world target.", "target args", "");
            case GOAL -> template(type, "Goal", "Run a registered pathfinding goal.", "goal args", "");
            case PATH -> template(type, "Path", "Follow or calculate path behavior.", "path args", "");
            case COME -> template(type, "Come", "Ask the bot to come to the player.", "", "");
            case SPACE_MOB -> template(type, "Space Mob", "Use the SpaceMob task from a script node.", "on|off [name] [distance] [tolerance] [radius] [track]", "on closest 3 0.35 32 track");
            case WALK -> template(type, "Walk", "Hold movement forward for a duration.", "duration", "1s");
            case JUMP -> template(type, "Jump", "Press jump for a duration.", "duration", "2t");
            case CRAWL -> template(type, "Crawl", "Hold crawl/sneak-style movement.", "duration", "1s");
            case CROUCH -> template(type, "Crouch", "Hold crouch for a duration.", "duration", "1s");
            case SPRINT -> template(type, "Sprint", "Hold sprint for a duration.", "duration", "1s");
            case PRESS_KEY -> template(type, "Press Key", "Press a named input key.", "key duration", "jump 2t");
            case LOOK -> template(type, "Look", "Rotate view toward a direction/target.", "args", "");
            case BREAK -> template(type, "Break", "Hold attack/break input.", "duration", "2t");
            case USE -> template(type, "Use", "Use item/interact input.", "duration", "2t");
            case INTERACT -> template(type, "Interact", "Interact with targeted block/entity.", "duration", "2t");
            case SWING -> template(type, "Swing", "Swing the player hand.", "", "");
            case PLACE_HAND -> template(type, "Place Hand", "Place using current hand.", "duration", "2t");
            case SET_VARIABLE -> template(type, "Set Variable", "Assign a variable.", "name value", "value 1");
            case CHANGE_VARIABLE -> template(type, "Change Variable", "Add a delta to a variable.", "name delta", "value 1");
            case CREATE_LIST -> template(type, "Create List", "Create an empty list variable.", "name", "items");
            case ADD_TO_LIST -> template(type, "Add To List", "Append a value to a list.", "list value", "items stone");
            case REMOVE_FIRST_FROM_LIST -> template(type, "Remove First", "Remove the first list value.", "list", "items");
            case REMOVE_LAST_FROM_LIST -> template(type, "Remove Last", "Remove the last list value.", "list", "items");
            case REMOVE_LIST_ITEM, REMOVE_FROM_LIST -> template(type, "Remove Item", "Remove an indexed list value.", "list index", "items 0");
            case LIST_ITEM -> template(type, "List Item", "Read an indexed list item.", "list index", "items 0");
            case LIST_LENGTH -> template(type, "List Length", "Read a list length.", "list", "items");
            case OPERATOR_RANDOM -> template(type, "Random", "Generate a random numeric value.", "min max", "0 10");
            case OPERATOR_MOD -> template(type, "Modulo", "Compute modulo.", "a b", "10 2");
            case STOP -> template(type, "Stop", "Stop player/path action.", "", "");
            case STOP_CHAIN -> template(type, "Stop Chain", "Stop current script chain.", "", "");
            case STOP_ALL -> template(type, "Stop All", "Stop all running chains/actions.", "", "");
            default -> template(type, prettify(type.id()), type.category().id() + " node.", "args", "");
        };
    }

    private static EbslNodeTemplate template(EbslNodeType type, String title, String description, String argsHint, String sampleArgs) {
        return new EbslNodeTemplate(type, title, description, argsHint, sampleArgs);
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
