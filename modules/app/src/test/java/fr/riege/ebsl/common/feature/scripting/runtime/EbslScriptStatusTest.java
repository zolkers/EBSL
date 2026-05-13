/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Copyright (C) 2026 Victor Riegert and EBSL contributors
 *
 * This file is part of EBSL.
 *
 * EBSL is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EBSL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EBSL. If not, see <https://www.gnu.org/licenses/>.
 */

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
