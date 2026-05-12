package fr.riege.ebsl.common.core.threading;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class EbslThreading {
    private static final Map<EbslThreadDomain, EbslExecutor> EXECUTORS = new EnumMap<>(EbslThreadDomain.class);

    static {
        for (EbslThreadDomain domain : EbslThreadDomain.values()) {
            EXECUTORS.put(domain, new EbslExecutor(domain, createExecutor(domain)));
        }
        Runtime.getRuntime().addShutdownHook(new Thread(EbslThreading::shutdown, "ebsl-threading-shutdown"));
    }

    private EbslThreading() {
    }

    public static EbslExecutor pathfinding() {
        return executor(EbslThreadDomain.PATHFINDING);
    }

    public static EbslExecutor modules() {
        return executor(EbslThreadDomain.MODULES);
    }

    public static EbslExecutor tasks() {
        return executor(EbslThreadDomain.TASKS);
    }

    public static EbslExecutor rendering() {
        return executor(EbslThreadDomain.RENDERING);
    }

    public static EbslExecutor io() {
        return executor(EbslThreadDomain.IO);
    }

    public static EbslExecutor general() {
        return executor(EbslThreadDomain.GENERAL);
    }

    public static EbslExecutor executor(EbslThreadDomain domain) {
        return EXECUTORS.get(domain);
    }

    public static EbslThreadException report(EbslThreadDomain domain, String owner, Throwable throwable) {
        return EbslThreadExceptionHandler.report(domain, owner, throwable);
    }

    public static void shutdown() {
        for (EbslExecutor executor : EXECUTORS.values()) {
            executor.shutdown();
        }
    }

    public static void shutdownNow() {
        for (EbslExecutor executor : EXECUTORS.values()) {
            executor.shutdownNow();
        }
    }

    private static ExecutorService createExecutor(EbslThreadDomain domain) {
        int threads = threadsFor(domain);
        return Executors.newFixedThreadPool(threads, new EbslThreadFactory(domain));
    }

    private static int threadsFor(EbslThreadDomain domain) {
        int cores = Runtime.getRuntime().availableProcessors();
        return switch (domain) {
            case PATHFINDING -> Math.max(1, cores / 2);
            case MODULES, TASKS -> Math.max(1, Math.min(4, cores / 3));
            case RENDERING, IO, GENERAL -> Math.max(1, Math.min(2, cores / 4));
        };
    }
}
