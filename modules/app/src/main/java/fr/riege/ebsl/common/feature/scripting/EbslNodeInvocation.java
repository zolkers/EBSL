package fr.riege.ebsl.common.feature.scripting;

import fr.riege.ebsl.common.feature.scripting.runtime.EbslRunner;
import fr.riege.ebsl.common.feature.scripting.runtime.EbslScriptRuntime;
import fr.riege.ebsl.common.feature.scripting.runtime.EbslRunner;
import fr.riege.ebsl.common.feature.scripting.runtime.EbslScriptRuntime;

import java.util.List;

public record EbslNodeInvocation(List<String> args, EbslScriptRuntime runtime, EbslRunner runner) {
    public EbslNodeInvocation {
        args = List.copyOf(args);
    }

    public boolean has(int index) {
        return index >= 0 && index < args.size();
    }

    public String arg(int index) {
        return args.get(index);
    }
}
