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
        EbslThreadErrorLog.record(domain, owner, threadName, exception);
        LOGGER.error("Unhandled EBSL {} task '{}' failed on {}", domain.id(), owner, threadName, exception.getCause());
        return exception;
    }
}
