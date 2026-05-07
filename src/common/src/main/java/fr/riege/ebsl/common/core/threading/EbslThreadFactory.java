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
