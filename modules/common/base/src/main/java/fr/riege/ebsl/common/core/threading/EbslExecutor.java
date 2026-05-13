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

package fr.riege.ebsl.common.core.threading;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

public final class EbslExecutor implements Executor {
    private final EbslThreadDomain domain;
    private final ExecutorService delegate;

    EbslExecutor(EbslThreadDomain domain, ExecutorService delegate) {
        this.domain = domain;
        this.delegate = delegate;
    }

    public EbslThreadDomain domain() {
        return domain;
    }

    @Override
    public void execute(Runnable command) {
        run("anonymous", command);
    }

    public CompletableFuture<Void> run(String owner, Runnable task) {
        return CompletableFuture.runAsync(wrap(owner, task), delegate);
    }

    public <T> CompletableFuture<T> supply(String owner, Supplier<T> task) {
        return CompletableFuture.supplyAsync(wrap(owner, task), delegate);
    }

    public void shutdown() {
        delegate.shutdown();
    }

    public void shutdownNow() {
        delegate.shutdownNow();
    }

    private Runnable wrap(String owner, Runnable task) {
        return () -> {
            try {
                task.run();
            } catch (Exception exception) {
                throw EbslThreadExceptionHandler.report(domain, owner, exception);
            }
        };
    }

    private <T> Supplier<T> wrap(String owner, Supplier<T> task) {
        return () -> {
            try {
                return task.get();
            } catch (Exception exception) {
                throw EbslThreadExceptionHandler.report(domain, owner, exception);
            }
        };
    }
}
