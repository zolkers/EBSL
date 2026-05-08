package fr.riege.ebsl.common.feature.scripting.enums;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public enum EbslNodeType {
    START(EbslNodeCategory.FLOW, true),
    START_CHAIN(EbslNodeCategory.FLOW, true),
    EVENT_FUNCTION(EbslNodeCategory.FLOW, true),
    EVENT_CALL(EbslNodeCategory.FLOW, true),
    VARIABLE(EbslNodeCategory.DATA, true),
    SET_VARIABLE(EbslNodeCategory.DATA, true),
    CHANGE_VARIABLE(EbslNodeCategory.DATA, true),
    CREATE_LIST(EbslNodeCategory.DATA, true),
    ADD_TO_LIST(EbslNodeCategory.DATA, true),
    REMOVE_FIRST_FROM_LIST(EbslNodeCategory.DATA, true),
    REMOVE_LAST_FROM_LIST(EbslNodeCategory.DATA, true),
    REMOVE_LIST_ITEM(EbslNodeCategory.DATA, true),
    REMOVE_FROM_LIST(EbslNodeCategory.DATA, true),
    LIST_ITEM(EbslNodeCategory.DATA, true),
    LIST_LENGTH(EbslNodeCategory.DATA, true),
    OPERATOR_EQUALS(EbslNodeCategory.DATA, true),
    OPERATOR_NOT(EbslNodeCategory.DATA, true),
    OPERATOR_BOOLEAN_NOT(EbslNodeCategory.DATA, true),
    OPERATOR_BOOLEAN_OR(EbslNodeCategory.DATA, true),
    OPERATOR_BOOLEAN_AND(EbslNodeCategory.DATA, true),
    OPERATOR_BOOLEAN_XOR(EbslNodeCategory.DATA, true),
    OPERATOR_GREATER(EbslNodeCategory.DATA, true),
    OPERATOR_LESS(EbslNodeCategory.DATA, true),
    OPERATOR_MOD(EbslNodeCategory.DATA, true),
    OPERATOR_RANDOM(EbslNodeCategory.DATA, true),
    GOTO(EbslNodeCategory.WORLD, true),
    GOAL_NEAREST_BLOCK(EbslNodeCategory.WORLD, true),
    TRAVEL(EbslNodeCategory.WORLD, true),
    GOAL(EbslNodeCategory.WORLD, false),
    PATH(EbslNodeCategory.WORLD, false),
    INVERT(EbslNodeCategory.WORLD, false),
    COME(EbslNodeCategory.WORLD, true),
    SURFACE(EbslNodeCategory.WORLD, false),
    COLLECT(EbslNodeCategory.WORLD, false),
    BUILD(EbslNodeCategory.WORLD, false),
    TUNNEL(EbslNodeCategory.WORLD, false),
    FARM(EbslNodeCategory.WORLD, false),
    PLACE(EbslNodeCategory.WORLD, false),
    CRAFT(EbslNodeCategory.WORLD, false),
    EXPLORE(EbslNodeCategory.WORLD, false),
    FOLLOW(EbslNodeCategory.WORLD, false),
    CONTROL_REPEAT(EbslNodeCategory.CONTROL, true),
    CONTROL_REPEAT_UNTIL(EbslNodeCategory.CONTROL, true),
    CONTROL_WAIT_UNTIL(EbslNodeCategory.CONTROL, true),
    CONTROL_FOREVER(EbslNodeCategory.CONTROL, true),
    CONTROL_IF(EbslNodeCategory.CONTROL, true),
    CONTROL_IF_ELSE(EbslNodeCategory.CONTROL, true),
    CONTROL_FORK(EbslNodeCategory.CONTROL, true),
    CONTROL_JOIN_ANY(EbslNodeCategory.CONTROL, true),
    CONTROL_JOIN_ALL(EbslNodeCategory.CONTROL, true),
    AIM_AT(EbslNodeCategory.PLAYER, true),
    AIM_AT_BLOCK(EbslNodeCategory.PLAYER, true),
    LOOK(EbslNodeCategory.PLAYER, true),
    WALK(EbslNodeCategory.PLAYER, true),
    JUMP(EbslNodeCategory.PLAYER, true),
    PRESS_KEY(EbslNodeCategory.PLAYER, true),
    CRAWL(EbslNodeCategory.PLAYER, true),
    CROUCH(EbslNodeCategory.PLAYER, true),
    SPRINT(EbslNodeCategory.PLAYER, true),
    FLY(EbslNodeCategory.PLAYER, false),
    STOP(EbslNodeCategory.PLAYER, true),
    SWING(EbslNodeCategory.PLAYER, true),
    USE(EbslNodeCategory.PLAYER, true),
    INTERACT(EbslNodeCategory.PLAYER, true),
    BREAK(EbslNodeCategory.PLAYER, true),
    BREAK_BLOCK(EbslNodeCategory.PLAYER, true),
    PLACE_HAND(EbslNodeCategory.PLAYER, true),
    TRADE(EbslNodeCategory.PLAYER, false),
    HOTBAR(EbslNodeCategory.PLAYER, false),
    SPACE_MOB(EbslNodeCategory.WORLD, true),
    DROP_ITEM(EbslNodeCategory.PLAYER, false),
    DROP_SLOT(EbslNodeCategory.PLAYER, false),
    CLICK_SLOT(EbslNodeCategory.PLAYER, false),
    CLICK_SCREEN(EbslNodeCategory.PLAYER, false),
    MOVE_ITEM(EbslNodeCategory.PLAYER, false),
    OPEN_INVENTORY(EbslNodeCategory.PLAYER, false),
    CLOSE_GUI(EbslNodeCategory.PLAYER, false),
    WRITE_BOOK(EbslNodeCategory.PLAYER, false),
    WRITE_SIGN(EbslNodeCategory.PLAYER, false),
    UI_UTILS(EbslNodeCategory.UTILITY, false),
    EQUIP_ARMOR(EbslNodeCategory.PLAYER, false),
    EQUIP_HAND(EbslNodeCategory.PLAYER, false),
    SENSOR_TOUCHING_BLOCK(EbslNodeCategory.SENSOR, false),
    SENSOR_TOUCHING_ENTITY(EbslNodeCategory.SENSOR, true),
    SENSOR_AT_COORDINATES(EbslNodeCategory.SENSOR, true),
    SENSOR_POSITION_OF(EbslNodeCategory.SENSOR, false),
    SENSOR_DISTANCE_BETWEEN(EbslNodeCategory.SENSOR, true),
    SENSOR_TARGETED_BLOCK(EbslNodeCategory.SENSOR, true),
    SENSOR_TARGETED_ENTITY(EbslNodeCategory.SENSOR, false),
    SENSOR_LOOK_DIRECTION(EbslNodeCategory.SENSOR, true),
    SENSOR_CURRENT_HAND(EbslNodeCategory.SENSOR, false),
    SENSOR_TARGETED_BLOCK_FACE(EbslNodeCategory.SENSOR, false),
    SENSOR_GUI_FILLED(EbslNodeCategory.SENSOR, false),
    SENSOR_IS_DAYTIME(EbslNodeCategory.SENSOR, false),
    SENSOR_IS_RAINING(EbslNodeCategory.SENSOR, false),
    SENSOR_HEALTH_BELOW(EbslNodeCategory.SENSOR, true),
    SENSOR_HUNGER_BELOW(EbslNodeCategory.SENSOR, false),
    SENSOR_ITEM_IN_INVENTORY(EbslNodeCategory.SENSOR, false),
    SENSOR_ITEM_IN_SLOT(EbslNodeCategory.SENSOR, false),
    SENSOR_SLOT_ITEM_COUNT(EbslNodeCategory.SENSOR, false),
    SENSOR_VILLAGER_TRADE(EbslNodeCategory.SENSOR, false),
    SENSOR_IN_STOCK(EbslNodeCategory.SENSOR, false),
    SENSOR_IS_SWIMMING(EbslNodeCategory.SENSOR, true),
    SENSOR_IS_IN_LAVA(EbslNodeCategory.SENSOR, true),
    SENSOR_IS_UNDERWATER(EbslNodeCategory.SENSOR, true),
    SENSOR_IS_ON_GROUND(EbslNodeCategory.SENSOR, true),
    SENSOR_IS_FALLING(EbslNodeCategory.SENSOR, true),
    SENSOR_IS_RENDERED(EbslNodeCategory.SENSOR, true),
    SENSOR_IS_VISIBLE(EbslNodeCategory.SENSOR, true),
    SENSOR_KEY_PRESSED(EbslNodeCategory.SENSOR, true),
    SENSOR_CHAT_MESSAGE(EbslNodeCategory.SENSOR, false),
    SENSOR_JOINED_SERVER(EbslNodeCategory.SENSOR, false),
    SENSOR_FABRIC_EVENT(EbslNodeCategory.SENSOR, false),
    SENSOR_ATTRIBUTE_DETECTION(EbslNodeCategory.SENSOR, false),
    RUN_PRESET(EbslNodeCategory.UTILITY, false),
    CUSTOM_NODE(EbslNodeCategory.UTILITY, false),
    WAIT(EbslNodeCategory.UTILITY, true),
    STICKY_NOTE(EbslNodeCategory.UTILITY, false),
    MESSAGE(EbslNodeCategory.UTILITY, true),
    TEMPLATE(EbslNodeCategory.UTILITY, false),
    STOP_CHAIN(EbslNodeCategory.UTILITY, true),
    STOP_ALL(EbslNodeCategory.UTILITY, true),
    PARAM_COORDINATE(EbslNodeCategory.DATA, true),
    PARAM_BLOCK(EbslNodeCategory.DATA, true),
    PARAM_ITEM(EbslNodeCategory.DATA, true),
    PARAM_VILLAGER_TRADE(EbslNodeCategory.DATA, false),
    PARAM_ENTITY(EbslNodeCategory.DATA, true),
    PARAM_PLAYER(EbslNodeCategory.DATA, true),
    PARAM_WAYPOINT(EbslNodeCategory.DATA, false),
    PARAM_SCHEMATIC(EbslNodeCategory.DATA, false),
    PARAM_INVENTORY_SLOT(EbslNodeCategory.DATA, false),
    PARAM_MESSAGE(EbslNodeCategory.DATA, true),
    PARAM_DURATION(EbslNodeCategory.DATA, true),
    PARAM_AMOUNT(EbslNodeCategory.DATA, true),
    PARAM_BOOLEAN(EbslNodeCategory.DATA, true),
    PARAM_HAND(EbslNodeCategory.DATA, true),
    PARAM_GUI(EbslNodeCategory.DATA, false),
    PARAM_KEY(EbslNodeCategory.DATA, true),
    PARAM_MOUSE_BUTTON(EbslNodeCategory.DATA, false),
    PARAM_RANGE(EbslNodeCategory.DATA, true),
    PARAM_DISTANCE(EbslNodeCategory.DATA, true),
    PARAM_DIRECTION(EbslNodeCategory.DATA, true),
    PARAM_BLOCK_FACE(EbslNodeCategory.DATA, false),
    PARAM_ROTATION(EbslNodeCategory.DATA, true),
    PARAM_PLACE_TARGET(EbslNodeCategory.DATA, false),
    PARAM_CLOSEST(EbslNodeCategory.DATA, true);

    private final EbslNodeCategory category;
    private final boolean executable;

    EbslNodeType(EbslNodeCategory category, boolean executable) {
        this.category = category;
        this.executable = executable;
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public EbslNodeCategory category() {
        return category;
    }

    public boolean executable() {
        return executable;
    }

    public static EbslNodeType byId(String id) {
        String normalized = normalize(id);
        for (EbslNodeType type : values()) {
            if (type.id().equals(normalized)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown EBSL task: " + id);
    }

    public static List<String> ids() {
        return Arrays.stream(values()).map(EbslNodeType::id).toList();
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT).replace('-', '_');
    }
}
