package fr.riege.ebsl.common.feature.scripting.blocks;

import fr.riege.ebsl.common.feature.scripting.runtime.EbslStatement;
import java.util.Optional;

public record EbslBlockStatementResult(boolean handled, EbslStatement statement) {
    static final EbslBlockStatementResult UNHANDLED = new EbslBlockStatementResult(false, null);

    static EbslBlockStatementResult handledWithoutStatement() {
        return new EbslBlockStatementResult(true, null);
    }

    static EbslBlockStatementResult statement(EbslStatement statement) {
        return new EbslBlockStatementResult(true, statement);
    }

    public Optional<EbslStatement> optionalStatement() {
        return Optional.ofNullable(statement);
    }
}
