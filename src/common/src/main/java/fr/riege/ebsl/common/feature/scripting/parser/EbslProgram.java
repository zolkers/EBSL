package fr.riege.ebsl.common.feature.scripting.parser;

import fr.riege.ebsl.common.feature.scripting.runtime.EbslStatement;
import java.util.List;
import java.util.Map;

public record EbslProgram(List<EbslStatement> statements, Map<String, List<EbslStatement>> functions) {
    public EbslProgram {
        statements = List.copyOf(statements);
        functions = Map.copyOf(functions);
    }
}
