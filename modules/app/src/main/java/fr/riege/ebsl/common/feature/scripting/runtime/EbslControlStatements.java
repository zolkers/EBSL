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

import java.util.List;

public final class EbslControlStatements {
    private EbslControlStatements() {
    }

    public record If(List<String> condition, List<EbslStatement> thenBlock, List<EbslStatement> elseBlock) implements EbslStatement {
        @Override
        public EbslStep tick(EbslScriptRuntime runtime, EbslRunner runner) {
            runner.call(runtime.condition(condition) ? thenBlock : elseBlock);
            return EbslStep.DONE;
        }
    }

    public static final class Repeat implements EbslStatement {
        private final List<String> countTokens;
        private final List<EbslStatement> body;
        private int remaining = -1;

        public Repeat(List<String> countTokens, List<EbslStatement> body) {
            this.countTokens = countTokens;
            this.body = body;
        }

        @Override
        public EbslStep tick(EbslScriptRuntime runtime, EbslRunner runner) {
            if (remaining < 0) {
                remaining = countTokens.isEmpty() ? 0 : (int) runtime.number(runtime.value(countTokens.get(0)));
            }
            if (remaining-- <= 0) {
                remaining = -1;
                return EbslStep.DONE;
            }
            runner.call(body);
            return EbslStep.RUNNING;
        }
    }

    public record Forever(List<EbslStatement> body) implements EbslStatement {
        @Override
        public EbslStep tick(EbslScriptRuntime runtime, EbslRunner runner) {
            runner.call(body);
            return EbslStep.RUNNING;
        }
    }

    public record RepeatUntil(List<String> condition, List<EbslStatement> body) implements EbslStatement {
        @Override
        public EbslStep tick(EbslScriptRuntime runtime, EbslRunner runner) {
            if (runtime.condition(condition)) {
                return EbslStep.DONE;
            }
            runner.call(body);
            return EbslStep.RUNNING;
        }
    }
}
