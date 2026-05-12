package fr.riege.ebsl.common.feature.scripting.runtime;

enum EbslScriptRunStatus {
    QUEUED("queued"),
    COMPILED("compiled"),
    RUNNING("running"),
    STOPPED("stopped"),
    COMPILE_ERROR("compile error"),
    RUNNER_STATUS("");

    private final String label;

    EbslScriptRunStatus(String label) {
        this.label = label;
    }

    String message() {
        return label;
    }

    String message(String detail) {
        if (this == RUNNER_STATUS) {
            return detail == null ? "" : detail;
        }
        return detail == null || detail.isBlank() ? label : label + ": " + detail;
    }
}
