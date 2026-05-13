package fr.riege.ebsl.common.api.threading;

import fr.riege.ebsl.common.api.EbslApi;
import fr.riege.ebsl.common.core.threading.EbslThreadDomain;
import fr.riege.ebsl.common.core.threading.EbslThreadErrorLog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
