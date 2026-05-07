package fr.riege.ebsl.common.feature.scripting.command;

import java.util.Arrays;
import java.util.List;

enum EbslCommandAction {
    RUN("run"),
    INLINE("inline"),
    STOP("stop"),
    STATUS("status"),
    TASKS("tasks");

    private final String id;

    EbslCommandAction(String id) {
        this.id = id;
    }

    String id() {
        return id;
    }

    static List<String> ids() {
        return Arrays.stream(values()).map(EbslCommandAction::id).toList();
    }
}
