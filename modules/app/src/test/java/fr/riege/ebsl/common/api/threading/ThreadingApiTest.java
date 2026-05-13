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

package fr.riege.ebsl.common.api.threading;

import fr.riege.ebsl.common.api.EbslApi;
import fr.riege.ebsl.common.core.threading.EbslThreadDomain;
import fr.riege.ebsl.common.core.threading.EbslThreadErrorLog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.*;

final class ThreadingApiTest {
    @AfterEach
    void clearErrors() {
        EbslThreadErrorLog.clear();
    }

    @Test
    void recordsManagedExecutorFailures() {
        var failure = EbslApi.threading()
            .executor(EbslThreadDomain.GENERAL)
            .run("test.failure", () -> {
                throw new IllegalStateException("boom");
            });
        assertThrows(CompletionException.class, failure.toCompletableFuture()::join);

        var errors = EbslApi.threading().errors();
        assertEquals(1, errors.size());
        assertEquals(EbslThreadDomain.GENERAL, errors.getFirst().domain());
        assertEquals("test.failure", errors.getFirst().owner());
        assertTrue(errors.getFirst().message().contains("boom"));
    }

    @Test
    void exposesDomains() {
        assertTrue(EbslApi.threading().domains().length >= 4);
    }
}
