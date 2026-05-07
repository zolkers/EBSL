package fr.riege.ebsl.common.core.threading;

public record EbslThreadError(
    long sequence,
    long capturedAtMs,
    EbslThreadDomain domain,
    String owner,
    String threadName,
    String exceptionClass,
    String message
) {
}
