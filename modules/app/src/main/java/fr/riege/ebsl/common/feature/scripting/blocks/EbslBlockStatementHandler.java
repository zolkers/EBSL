package fr.riege.ebsl.common.feature.scripting.blocks;

/**
 * Defines the contract for {@code EbslBlockStatementHandler} implementations.
 */
@FunctionalInterface
public interface EbslBlockStatementHandler {
    EbslBlockStatementResult parse(EbslBlockStatementInvocation invocation);
}
