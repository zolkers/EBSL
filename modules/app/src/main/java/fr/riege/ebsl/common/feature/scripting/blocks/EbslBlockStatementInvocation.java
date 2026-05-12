package fr.riege.ebsl.common.feature.scripting.blocks;

import fr.riege.ebsl.common.feature.scripting.parser.EbslParser;
import fr.riege.ebsl.common.feature.scripting.runtime.EbslStatement;
import java.util.List;

public record EbslBlockStatementInvocation(
    String command,
    List<String> args,
    List<EbslStatement> body,
    EbslParser parser
) {
    public EbslBlockStatementInvocation {
        args = List.copyOf(args);
        body = List.copyOf(body);
    }

    void defineFunction() {
        if (!args.isEmpty()) {
            parser.defineFunction(args.get(0), body);
        }
    }

    List<EbslStatement> readOptionalElse() {
        return parser.readOptionalElse();
    }
}
