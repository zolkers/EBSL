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
