package fr.riege.ebsl.common.core.threading;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public final class EbslThreadErrorLog {
    private static final int MAX_ERRORS = 256;
    private static final Object LOCK = new Object();
    private static final AtomicLong SEQUENCE = new AtomicLong();
    private static final Deque<EbslThreadError> ERRORS = new ArrayDeque<>(MAX_ERRORS);

    private EbslThreadErrorLog() {
    }

    static void recordError(EbslThreadDomain domain, String owner, String threadName, Throwable throwable) {
        Throwable cause = throwable instanceof EbslThreadException e && e.getCause() != null
            ? e.getCause()
            : throwable;
        EbslThreadError error = new EbslThreadError(
            SEQUENCE.incrementAndGet(),
            System.currentTimeMillis(),
            domain,
            owner,
            threadName,
            cause.getClass().getName(),
            cause.getMessage() != null ? cause.getMessage() : "");
        synchronized (LOCK) {
            if (ERRORS.size() >= MAX_ERRORS) {
                ERRORS.pollFirst();
            }
            ERRORS.addLast(error);
        }
    }

    public static List<EbslThreadError> snapshot() {
        synchronized (LOCK) {
            return new ArrayList<>(ERRORS);
        }
    }

    public static void clear() {
        synchronized (LOCK) {
            ERRORS.clear();
        }
    }
}
