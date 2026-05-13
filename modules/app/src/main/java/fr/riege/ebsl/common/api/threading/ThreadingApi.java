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

package fr.riege.ebsl.common.api.threading;

import fr.riege.ebsl.common.api.core.annotation.EbslApiOperation;
import fr.riege.ebsl.common.api.core.annotation.EbslApiSurface;
import fr.riege.ebsl.common.core.threading.*;

import java.util.List;

@EbslApiSurface(EbslApiSurface.Domain.THREADING)
public final class ThreadingApi {
    @EbslApiOperation("Read supported EBSL thread domains.")
    public EbslThreadDomain[] domains() {
        return EbslThreadDomain.values();
    }

    @EbslApiOperation("Access an executor for a thread domain.")
    public EbslExecutor executor(EbslThreadDomain domain) {
        return EbslThreading.executor(domain);
    }

    @EbslApiOperation("Read recent thread failures.")
    public List<EbslThreadError> errors() {
        return EbslThreadErrorLog.snapshot();
    }

    @EbslApiOperation("Clear recent thread failures.")
    public void clearErrors() {
        EbslThreadErrorLog.clear();
    }
}
