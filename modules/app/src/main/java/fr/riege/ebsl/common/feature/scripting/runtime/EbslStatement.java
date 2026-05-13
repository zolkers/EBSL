package fr.riege.ebsl.common.feature.scripting.runtime;

/**
 * Defines the contract for {@code EbslStatement} implementations.
 */
public interface EbslStatement {
    EbslStep tick(EbslScriptRuntime runtime, EbslRunner runner);
}
