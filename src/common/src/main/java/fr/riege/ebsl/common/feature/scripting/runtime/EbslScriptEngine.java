package fr.riege.ebsl.common.feature.scripting.runtime;

import fr.riege.ebsl.common.feature.scripting.parser.EbslParser;
import fr.riege.ebsl.common.feature.scripting.parser.EbslProgram;
import fr.riege.ebsl.common.feature.scripting.parser.EbslTokenizer;
import fr.riege.ebsl.common.platform.EbslPlatform;

public final class EbslScriptEngine {
    private EbslScriptEngine() {
    }

    public static EbslProgram compile(String source) {
        return new EbslParser(EbslTokenizer.tokenize(source)).parse();
    }

    public static EbslRunner runner(EbslProgram program, EbslPlatform platform) {
        return new EbslRunner(program, platform);
    }
}
