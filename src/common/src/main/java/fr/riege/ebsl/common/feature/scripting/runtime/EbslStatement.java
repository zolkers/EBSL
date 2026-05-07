package fr.riege.ebsl.common.feature.scripting.runtime;

public interface EbslStatement {
    EbslStep tick(EbslScriptRuntime runtime, EbslRunner runner);
}
