package fr.riege.ebsl.common.feature.scripting.runtime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class EbslScriptStatusTest {
    @Test
    void taskStatusFormatsDetailOnlyWhenPresent() {
        assertEquals("idle", EbslScriptTaskStatus.IDLE.message());
        assertEquals("loaded main.ebsl", EbslScriptTaskStatus.LOADED.message("main.ebsl"));
        assertEquals("no script matched missing", EbslScriptTaskStatus.NO_SCRIPT_MATCHED.message("missing"));
        assertEquals("inline#1 [inline] stopped", EbslScriptTaskStatus.SUMMARY.message("inline#1 [inline] stopped"));
        assertEquals("", EbslScriptTaskStatus.SUMMARY.message(null));
    }

    @Test
    void runStatusUsesColonForOwnDetailsAndPassthroughForRunnerStatus() {
        assertEquals("queued", EbslScriptRunStatus.QUEUED.message());
        assertEquals("compile error: line 2", EbslScriptRunStatus.COMPILE_ERROR.message("line 2"));
        assertEquals("waiting", EbslScriptRunStatus.RUNNER_STATUS.message("waiting"));
        assertEquals("", EbslScriptRunStatus.RUNNER_STATUS.message(null));
    }
}
