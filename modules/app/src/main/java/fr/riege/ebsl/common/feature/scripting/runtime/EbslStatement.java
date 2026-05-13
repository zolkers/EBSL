package fr.riege.ebsl.common.feature.scripting.runtime;

/**
 * Represents an executable statement in the script runtime.
 *
 * <p>Statements advance by ticks and report the next execution step to the runner, allowing waits, branches, and commands to share one scheduler.</p>
 */
public interface EbslStatement {
    /**
     * Advances this component by one runtime tick.
 *
     * @param runtime the active script runtime
     * @param runner the runner coordinating statement execution
     * @return the value defined by this contract
     */
    EbslStep tick(EbslScriptRuntime runtime, EbslRunner runner);
}
