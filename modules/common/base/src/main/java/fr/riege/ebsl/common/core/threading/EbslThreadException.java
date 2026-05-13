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

public final class EbslThreadException extends RuntimeException {
    private final EbslThreadDomain domain;
    private final String owner;
    private final String threadName;

    public EbslThreadException(EbslThreadDomain domain, String owner, String threadName, Throwable cause) {
        super("Unhandled EBSL thread exception in " + domain.id() + " task " + owner + " on " + threadName, cause);
        this.domain = domain;
        this.owner = owner;
        this.threadName = threadName;
    }

    public EbslThreadDomain domain() {
        return domain;
    }

    public String owner() {
        return owner;
    }

    public String threadName() {
        return threadName;
    }
}
