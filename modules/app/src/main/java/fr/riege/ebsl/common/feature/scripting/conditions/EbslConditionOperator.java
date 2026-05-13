package fr.riege.ebsl.common.feature.scripting.conditions;

import fr.riege.ebsl.common.feature.scripting.runtime.EbslScriptRuntime;
/**
 * Defines the contract for {@code EbslConditionOperator} implementations.
 */
@FunctionalInterface
public interface EbslConditionOperator {
    boolean evaluate(EbslScriptRuntime runtime, Object left, Object right);
}
