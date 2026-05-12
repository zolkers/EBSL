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
            } catch (Throwable throwable) {
                throw EbslThreadExceptionHandler.report(domain, owner, throwable);
            }
        };
    }

    private <T> Supplier<T> wrap(String owner, Supplier<T> task) {
        return () -> {
            try {
                return task.get();
            } catch (Throwable throwable) {
                throw EbslThreadExceptionHandler.report(domain, owner, throwable);
            }
        };
    }
}
