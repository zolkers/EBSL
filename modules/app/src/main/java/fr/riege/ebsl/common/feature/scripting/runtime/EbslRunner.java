/*
 * This file is part of EBSL.
 *
 * EBSL is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * EBSL is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package fr.riege.ebsl.common.feature.scripting.runtime;

import fr.riege.ebsl.common.feature.scripting.parser.EbslProgram;
import fr.riege.ebsl.common.platform.EbslPlatform;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Locale;

public final class EbslRunner {
    private static final int MAX_STEPS_PER_TICK = 64;

    private final EbslProgram program;
    private final EbslScriptRuntime runtime;
    private final ArrayDeque<Frame> frames = new ArrayDeque<>();
    private boolean started;
    private boolean done;
    private String status = "idle";

    EbslRunner(EbslProgram program, EbslPlatform platform) {
        this.program = program;
        this.runtime = new EbslScriptRuntime(platform);
    }

    public void start() {
        frames.clear();
        frames.push(new Frame(program.statements()));
        started = true;
        done = false;
        status = "running";
    }

    public void tick() {
        if (!started) {
            start();
        }
        if (done) {
            return;
        }
        if (runtime.stopped()) {
            finish("stopped");
            return;
        }
        runBudgetedSteps();
        if (frames.isEmpty()) {
            finish("done");
        }
    }

    public void stop() {
        runtime.stop();
        finish("stopped");
    }

    public boolean done() {
        return done;
    }

    public String status() {
        return status;
    }

    public void call(List<EbslStatement> statements) {
        frames.push(new Frame(statements));
    }

    public List<EbslStatement> function(String name) {
        return program.functions().get(name.toLowerCase(Locale.ROOT));
    }

    private void runBudgetedSteps() {
        int budget = MAX_STEPS_PER_TICK;
        while (budget-- > 0 && !frames.isEmpty() && !runtime.stopped()) {
            Frame frame = frames.peek();
            if (frame.finished()) {
                frames.pop();
                continue;
            }
            if (frame.current().tick(runtime, this) == EbslStep.RUNNING) {
                return;
            }
            frame.advance();
        }
    }

    private void finish(String status) {
        done = true;
        this.status = status;
    }

    private static final class Frame {
        private final List<EbslStatement> statements;
        private int pc;

        private Frame(List<EbslStatement> statements) {
            this.statements = statements;
        }

        private boolean finished() {
            return pc >= statements.size();
        }

        private EbslStatement current() {
            return statements.get(pc);
        }

        private void advance() {
            pc++;
        }
    }
}
