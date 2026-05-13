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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class EbslThreadExceptionHandler implements Thread.UncaughtExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger("ebsl-threading");

    private final EbslThreadDomain domain;

    EbslThreadExceptionHandler(EbslThreadDomain domain) {
        this.domain = domain;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        report(domain, "uncaught", thread.getName(), throwable);
    }

    static EbslThreadException report(EbslThreadDomain domain, String owner, Throwable throwable) {
        return report(domain, owner, Thread.currentThread().getName(), throwable);
    }

    static EbslThreadException report(EbslThreadDomain domain, String owner, String threadName, Throwable throwable) {
        EbslThreadException exception = throwable instanceof EbslThreadException e
            ? e
            : new EbslThreadException(domain, owner, threadName, throwable);
        EbslThreadErrorLog.recordError(domain, owner, threadName, exception);
        LOGGER.error("Unhandled EBSL {} task '{}' failed on {}", domain.id(), owner, threadName, exception.getCause());
        return exception;
    }
}
