package fr.riege.ebsl.common.feature.scripting.conditions;

import fr.riege.ebsl.common.feature.scripting.runtime.EbslScriptRuntime;
@FunctionalInterface
public interface EbslConditionOperator {
    boolean evaluate(EbslScriptRuntime runtime, Object left, Object right);
}
