package fr.riege.ebsl.common.feature.scripting.blocks;

@FunctionalInterface
public interface EbslBlockStatementHandler {
    EbslBlockStatementResult parse(EbslBlockStatementInvocation invocation);
}
