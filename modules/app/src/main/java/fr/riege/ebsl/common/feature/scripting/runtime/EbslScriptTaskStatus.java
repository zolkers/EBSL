package fr.riege.ebsl.common.feature.scripting.runtime;

enum EbslScriptTaskStatus {
    IDLE("idle"),
    RUNNING("running"),
    LOADED("loaded"),
    STOPPED("stopped"),
    NO_SCRIPT_MATCHED("no script matched"),
    SUMMARY("");

    private final String label;

    EbslScriptTaskStatus(String label) {
        this.label = label;
    }

    String message() {
        return label;
    }

    String message(String detail) {
        if (this == SUMMARY) {
            return detail == null ? "" : detail;
        }
        return detail == null || detail.isBlank() ? label : label + " " + detail;
    }
}
