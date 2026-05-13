package fr.riege.ebsl.common.feature.scripting.conditions;

import fr.riege.ebsl.common.feature.scripting.runtime.EbslScriptRuntime;
/**
 * Evaluates one binary condition operator for the EBSL runtime.
 *
 * <p>Operators receive already resolved operands and must return a deterministic boolean for the current runtime snapshot.</p>
 */
@FunctionalInterface
public interface EbslConditionOperator {
    /**
     * Evaluates this contract against the supplied context.
 *
     * @param runtime the active script runtime
     * @param left the left operand
     * @param right the right operand
     * @return true when the condition is satisfied; false otherwise
     */
    boolean evaluate(EbslScriptRuntime runtime, Object left, Object right);
}
