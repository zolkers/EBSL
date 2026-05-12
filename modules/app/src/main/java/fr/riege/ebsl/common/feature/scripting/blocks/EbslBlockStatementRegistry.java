package fr.riege.ebsl.common.feature.scripting.blocks;

import fr.riege.ebsl.common.core.registry.MapRegistry;
import fr.riege.ebsl.common.feature.scripting.runtime.EbslControlStatements;
import java.util.Locale;

public final class EbslBlockStatementRegistry {
    private static final MapRegistry<String, EbslBlockStatementHandler> HANDLERS = new MapRegistry<>(null);

    static {
        register(EbslBlockStatementType.EVENT_FUNCTION, invocation -> {
            invocation.defineFunction();
            return EbslBlockStatementResult.handledWithoutStatement();
        });
        register(EbslBlockStatementType.IF, invocation ->
            EbslBlockStatementResult.statement(new EbslControlStatements.If(invocation.args(), invocation.body(), invocation.readOptionalElse())));
        register(EbslBlockStatementType.REPEAT, invocation ->
            EbslBlockStatementResult.statement(new EbslControlStatements.Repeat(invocation.args(), invocation.body())));
        register(EbslBlockStatementType.FOREVER, invocation ->
            EbslBlockStatementResult.statement(new EbslControlStatements.Forever(invocation.body())));
        register(EbslBlockStatementType.REPEAT_UNTIL, invocation ->
            EbslBlockStatementResult.statement(new EbslControlStatements.RepeatUntil(invocation.args(), invocation.body())));
    }

    private EbslBlockStatementRegistry() {
    }

    public static EbslBlockStatementResult parse(EbslBlockStatementInvocation invocation) {
        EbslBlockStatementHandler handler = HANDLERS.get(normalize(invocation.command()));
        return handler == null ? EbslBlockStatementResult.UNHANDLED : handler.parse(invocation);
    }

    private static void register(EbslBlockStatementType type, EbslBlockStatementHandler handler) {
        HANDLERS.register(normalize(type.id()), handler);
        for (String alias : type.aliases()) {
            HANDLERS.register(normalize(alias), handler);
        }
    }

    private static String normalize(String token) {
        return token == null ? "" : token.trim().toLowerCase(Locale.ROOT).replace('-', '_');
    }
}
