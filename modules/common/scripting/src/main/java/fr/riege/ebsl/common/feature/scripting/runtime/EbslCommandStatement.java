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

import fr.riege.ebsl.common.feature.scripting.EbslNode;
import fr.riege.ebsl.common.feature.scripting.EbslNodeInvocation;
import fr.riege.ebsl.common.feature.registry.ScriptingRegistries;
import java.util.List;

public final class EbslCommandStatement implements EbslStatement {
    private final List<String> args;
    private final EbslNode node;
    private boolean started;
    private int ticksLeft;

    public EbslCommandStatement(String command, List<String> args) {
        this.args = List.copyOf(args);
        this.node = ScriptingRegistries.scripting().nodes().create(command);
    }

    @Override
    public EbslStep tick(EbslScriptRuntime runtime, EbslRunner runner) {
        if (node != null && node.isWaitUntil()) {
            return runtime.condition(args) ? EbslStep.DONE : EbslStep.RUNNING;
        }
        if (!started) {
            start(runtime, runner);
        }
        if (isTimedNodeRunning(runtime, runner)) {
            return EbslStep.RUNNING;
        }

        if (node != null && node.waitsForNavigation() && runtime.navigation().isNavigating()) {
            return EbslStep.RUNNING;
        }
        if (node != null && node.releasesGameplayKeys()) {
            runtime.platform().input().releaseGameplayKeys();
        }
        if (node != null) {
            node.finish(new EbslNodeInvocation(args, runtime, runner));
        }
        started = false;
        return EbslStep.DONE;
    }

    private void start(EbslScriptRuntime runtime, EbslRunner runner) {
        started = true;
        ticksLeft = node == null ? 0 : node.start(new EbslNodeInvocation(args, runtime, runner));
    }

    private boolean isTimedNodeRunning(EbslScriptRuntime runtime, EbslRunner runner) {
        if (ticksLeft <= 0 || node == null) {
            return false;
        }
        EbslNodeInvocation invocation = new EbslNodeInvocation(args, runtime, runner);
        node.tick(invocation);
        if (node.isComplete(invocation)) {
            ticksLeft = 0;
            return false;
        }
        ticksLeft--;
        return true;
    }
}
