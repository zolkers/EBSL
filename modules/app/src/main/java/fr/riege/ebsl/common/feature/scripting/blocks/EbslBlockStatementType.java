package fr.riege.ebsl.common.feature.scripting.blocks;

import java.util.List;

public enum EbslBlockStatementType {
    EVENT_FUNCTION("event_function"),
    IF("if", "control_if", "control_if_else"),
    REPEAT("repeat", "control_repeat"),
    FOREVER("forever", "control_forever"),
    REPEAT_UNTIL("repeat_until", "control_repeat_until");

    private final String id;
    private final List<String> aliases;

    EbslBlockStatementType(String id, String... aliases) {
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
