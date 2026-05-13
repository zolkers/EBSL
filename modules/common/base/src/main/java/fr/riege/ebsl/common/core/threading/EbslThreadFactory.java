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
package fr.riege.ebsl.common.core.threading;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

final class EbslThreadFactory implements ThreadFactory {
    private final EbslThreadDomain domain;
    private final AtomicInteger nextId = new AtomicInteger();

    EbslThreadFactory(EbslThreadDomain domain) {
        this.domain = domain;
    }

    @Override
    public Thread newThread(Runnable runnable) {
        Thread thread = new Thread(runnable, "ebsl-" + domain.id() + "-" + nextId.incrementAndGet());
        thread.setDaemon(true);
        thread.setUncaughtExceptionHandler(new EbslThreadExceptionHandler(domain));
        return thread;
    }
}
