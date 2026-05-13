package fr.riege.ebsl.common.feature.scripting.blocks;

/**
 * Parses one block-style statement in the EBSL scripting language.
 *
 * <p>Handlers convert source-level block invocations into structured parser results while keeping grammar extensions isolated.</p>
 */
@FunctionalInterface
public interface EbslBlockStatementHandler {
    /**
     * Parses the supplied block statement invocation.
 *
     * @param invocation the invocation state for the current script or parser operation
     * @return the value defined by this contract
     */
    EbslBlockStatementResult parse(EbslBlockStatementInvocation invocation);
}
