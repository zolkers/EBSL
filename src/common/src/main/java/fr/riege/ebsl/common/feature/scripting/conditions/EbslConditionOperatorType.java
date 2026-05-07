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

    String id() {
        return id;
    }

    List<String> aliases() {
        return aliases;
    }
}
